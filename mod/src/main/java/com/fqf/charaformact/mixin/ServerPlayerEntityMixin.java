package com.fqf.charaformact.mixin;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.cfadata.CfaServerPlayerData;
import com.fqf.charaformact.cfadata.injections.AdvCfaServerDataHolder;
import com.fqf.charaformact.packets.CfaDataPackets;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.util.CfaNbtKeys;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
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
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements AdvCfaServerDataHolder {
	@Shadow public ServerPlayNetworkHandler networkHandler;

	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Unique private long tickAfterStomp;

	@Override
	public void move(MovementType movementType, Vec3d movement) {
		CfaServerPlayerData data = this.cfa$getCfaData();
		Vec3d oldMovement = movement;
		Vec3d oldPos = this.getPos();
		// Only perform stomp checks on movement that comes from a player packet (as opposed to server-side travel).
		// Should this change??
		long time = this.getWorld().getTime();
		if(data.isEnabled() && data.doCustomTravel() && data.getAction().COLLISION_ATTACK_TYPE != null
				&& (movementType == MovementType.PLAYER || movementType == MovementType.SELF)
				&& time != this.tickAfterStomp && time != this.tickAfterStomp - 1)
			movement = data.getAction().COLLISION_ATTACK_TYPE.moveHook(data, movement);

		super.move(movementType, movement);
		if(!oldMovement.equals(movement)) {
//			CharaFormAct.LOGGER.info("Server-sided stomp after move completion:\nTick: {}\tForbiddenTick: {}\tMovetype: {}\nOld position: {}\nMovement: {}\nNew position: {}\nDifference: {}",
//					this.getWorld().getTime(), this.tickAfterStomp, movementType, oldPos, movement, this.getPos(), this.getPos().subtract(oldPos));
			this.tickAfterStomp = time + 1;
		}
//		else CharaFormAct.LOGGER.info("Movement: {}->{}", oldPos, this.getPos());
	}

	@Inject(method = "readCustomDataFromNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
	private void readCfaDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
		super.readCustomDataFromNbt(nbt);

		if(nbt.contains(CfaNbtKeys.DATA, NbtElement.COMPOUND_TYPE)) {
			NbtCompound persistentCfaData = nbt.getCompound(CfaNbtKeys.DATA);

			boolean extraLogging = CharaFormAct.CONFIG.logNBTReadWrite();
			if(extraLogging) CharaFormAct.LOGGER.info("Reading player NBT:\nEnabled: {}\nCharacter: {}\nPower-up: {}",
					persistentCfaData.getBoolean(CfaNbtKeys.ENABLED),
					persistentCfaData.getString(CfaNbtKeys.CHARACTER),
					persistentCfaData.getString(CfaNbtKeys.POWER_FORM));

			if(persistentCfaData.getBoolean(CfaNbtKeys.ENABLED)) {
				String storedCharacterID = persistentCfaData.getString(CfaNbtKeys.CHARACTER);
				if(storedCharacterID.isEmpty()) {
					CharaFormAct.LOGGER.error("Shocking error: A player's NBT data claims the mod is enabled, but no character ID is stored?!");
				}
				else if(RegistryManager.CHARACTERS.containsId(Identifier.of(storedCharacterID))) {
					ParsedCharacter storedCharacter = Objects.requireNonNull(RegistryManager.CHARACTERS.get(Identifier.of(storedCharacterID)));
					String storedPowerUpID = persistentCfaData.getString(CfaNbtKeys.POWER_FORM);
					if(storedPowerUpID.isEmpty()) {
						CharaFormAct.LOGGER.error("Shocking error: A player's NBT data claims the mod is enabled, and a character ID is stored, but no power-up ID is stored?!");
					}
					else {
						if(!RegistryManager.FORMS.containsId(Identifier.of(storedPowerUpID))) {
							CharaFormAct.LOGGER.error("A player's NBT data contains an invalid Power-up ID: {}." +
									"The player will instead be set to their character's default power-up state.", storedPowerUpID);
							storedPowerUpID = storedCharacter.INITIAL_FORM.ID.toString();
						}

						if(extraLogging)
							CharaFormAct.LOGGER.info("Loaded a full set of CFA Data from NBT. This is {} in {} form.", storedCharacterID, storedPowerUpID);

						CfaServerPlayerData data = this.cfa$getCfaData();
						if(this.networkHandler == null) {
							if(extraLogging)
								CharaFormAct.LOGGER.info("Player is not yet ready for networking. Assigning silently for later synchronization...");
							data.setupVariablesBeforeInitialApply(
									storedCharacter,
									RegistryManager.FORMS.get(Identifier.of(storedPowerUpID))
							);
						}
						else {
							if(extraLogging) CharaFormAct.LOGGER.info("Syncing data from NBT...");
							data.assignCharacter(storedCharacterID);
							data.assignForm(storedPowerUpID);
						}
					}
				}
				else CharaFormAct.LOGGER.error("A player's NBT data contains an invalid Character ID: {}", storedCharacterID);
			}
		}
	}

	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player) {
		super.onStartedTrackingBy(player);
		CfaDataPackets.syncCfaDataToPlayerS2C((ServerPlayerEntity) (Object) this, player);
	}
}
