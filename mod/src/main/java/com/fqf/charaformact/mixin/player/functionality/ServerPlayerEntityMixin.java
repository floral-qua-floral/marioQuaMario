package com.fqf.charaformact.mixin.player.functionality;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.cfadata.CfaServerPlayerData;
import com.fqf.charaformact.cfadata.injections.AdvCfaServerDataHolder;
import com.fqf.charaformact.packets.CfaDataPackets;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.util.CfaGamerules;
import com.fqf.charaformact.util.CfaNbtKeys;
import com.fqf.charaformact.util.EntitiesMixinInterface;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements AdvCfaServerDataHolder, EntitiesMixinInterface {
	@Shadow public ServerPlayNetworkHandler networkHandler;
	@Shadow public abstract ServerWorld getServerWorld();

	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Unique private long ticksAfterCollisionAttack;

	@Override
	public void move(MovementType movementType, Vec3d movement) {
		CfaServerPlayerData data = this.cfa$getCfaData();
		Vec3d oldMovement = movement;
		// Only perform collision attack checks on movement that comes from a player packet (as opposed to server-side travel).
		// Should this change??
		long time = this.getWorld().getTime();
		if(data.isEnabled() && data.doCustomTravel(false) && data.getAction().COLLISION_ATTACK_TYPE != null
				&& (movementType == MovementType.PLAYER || movementType == MovementType.SELF)
				&& time != this.ticksAfterCollisionAttack && time != this.ticksAfterCollisionAttack - 1)
			movement = data.getAction().COLLISION_ATTACK_TYPE.moveHook(data, movement);

		super.move(movementType, movement);
		if(!oldMovement.equals(movement)) this.ticksAfterCollisionAttack = time + 1;
	}

	@Inject(method = "readCustomDataFromNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
	private void readCfaDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
		super.readCustomDataFromNbt(nbt);

		if(nbt.contains(CfaNbtKeys.DATA, NbtElement.COMPOUND_TYPE)) {
			NbtCompound persistentCfaData = nbt.getCompound(CfaNbtKeys.DATA);

			boolean extraLogging = CharaFormAct.CONFIG.logNBTReadWrite();
			if(extraLogging) CharaFormAct.LOGGER.info("Reading player NBT:\nEnabled: {}\nCharacter: {}\nForm: {}",
					persistentCfaData.getBoolean(CfaNbtKeys.ENABLED),
					persistentCfaData.getString(CfaNbtKeys.CHARACTER),
					persistentCfaData.getString(CfaNbtKeys.FORM));

			if(persistentCfaData.getBoolean(CfaNbtKeys.ENABLED)) {
				String storedCharacterID = persistentCfaData.getString(CfaNbtKeys.CHARACTER);
				if(storedCharacterID.isEmpty()) {
					CharaFormAct.LOGGER.error("Shocking error: A player's NBT data claims the mod is enabled, but no character ID is stored?!");
				}
				else if(RegistryManager.CHARACTERS.containsId(Identifier.of(storedCharacterID))) {
					ParsedCharacter storedCharacter = Objects.requireNonNull(RegistryManager.CHARACTERS.get(Identifier.of(storedCharacterID)));
					String storedFormID = persistentCfaData.getString(CfaNbtKeys.FORM);
					if(storedFormID.isEmpty()) {
						CharaFormAct.LOGGER.error("Shocking error: A player's NBT data claims the mod is enabled, and a character ID is stored, but no form ID is stored?!");
					}
					else {
						if(!RegistryManager.FORMS.containsId(Identifier.of(storedFormID))) {
							CharaFormAct.LOGGER.error("A player's NBT data contains an invalid Form ID: {}." +
									" The player will instead be set to their character's default form state.", storedFormID);
							storedFormID = storedCharacter.INITIAL_FORM.ID.toString();
						}

						if(extraLogging)
							CharaFormAct.LOGGER.info("Loaded a full set of CFA Data from NBT. This is {} in {} form.", storedCharacterID, storedFormID);

						CfaServerPlayerData data = this.cfa$getCfaData();
						if(this.networkHandler == null) {
							if(extraLogging)
								CharaFormAct.LOGGER.info("Player is not yet ready for networking. Assigning silently for later synchronization...");
							data.setupVariablesBeforeInitialApply(
									storedCharacter,
									RegistryManager.FORMS.get(Identifier.of(storedFormID))
							);

							// Barebones fix for MC-17876. Other mods fix it more robustly, but having at least this
							// minimal fix is basically mandatory for CFA's "multiple health bars" functionality, so I
							// put it here.
							if(nbt.contains("Health", NbtElement.NUMBER_TYPE))
								data.initialHealth = nbt.getFloat("Health");
						}
						else {
							if(extraLogging) CharaFormAct.LOGGER.info("Syncing data from NBT...");
							data.assignCharacter(storedCharacterID);
							data.assignForm(storedFormID);
						}
					}
				}
				else CharaFormAct.LOGGER.error("A player's NBT data contains an invalid Character ID: {}", storedCharacterID);
			}
		}
	}

	@WrapOperation(method="requestTeleportAndDismount", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;setPosition(DDD)V"))
	private void preventSetPositionWhileDismounting(ServerPlayerEntity instance, double x, double y, double z, Operation<Void> original) {
		CfaServerPlayerData data = this.cfa$getCfaData();
		if(data.doDismountRepositioning())
			original.call(instance, x, y, z);
	}

	@Override
	public void cfa$onStartedTrackingBy(ServerPlayerEntity player) {
		CfaDataPackets.syncCfaDataToPlayerS2C((ServerPlayerEntity) (Object) this, player);
	}

	@Override
	public float cfa$modifyDamageRightBeforeApplication(float original) {
		CfaServerPlayerData data = this.cfa$getCfaData();
		if(data.getHealthBarCount() <= 1) return original;

		float damageToRevert = data.getHealthWithinForm();
		if(original <= damageToRevert) return original;

		float overkillDamage = (original - damageToRevert)
				* (float) this.getServerWorld().getGameRules().get(CfaGamerules.DAMAGE_PAST_REVERSION_MULTIPLIER).get();

		return damageToRevert + overkillDamage;
	}
}
