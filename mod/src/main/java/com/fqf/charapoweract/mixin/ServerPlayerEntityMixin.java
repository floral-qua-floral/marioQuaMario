package com.fqf.charapoweract.mixin;

import com.fqf.charapoweract.CharaPowerAct;
import com.fqf.charapoweract.cpadata.CPAServerPlayerData;
import com.fqf.charapoweract.cpadata.injections.AdvCPAServerDataHolder;
import com.fqf.charapoweract.packets.CPADataPackets;
import com.fqf.charapoweract.registries.RegistryManager;
import com.fqf.charapoweract.registries.power_granting.ParsedCharacter;
import com.fqf.charapoweract.util.CPANbtKeys;
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
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements AdvCPAServerDataHolder {
	@Shadow public ServerPlayNetworkHandler networkHandler;

	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Unique private long tickAfterStomp;

	@Override
	public void move(MovementType movementType, Vec3d movement) {
		CPAServerPlayerData data = this.cpa$getCPAData();
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
//			CharaPowerAct.LOGGER.info("Server-sided stomp after move completion:\nTick: {}\tForbiddenTick: {}\tMovetype: {}\nOld position: {}\nMovement: {}\nNew position: {}\nDifference: {}",
//					this.getWorld().getTime(), this.tickAfterStomp, movementType, oldPos, movement, this.getPos(), this.getPos().subtract(oldPos));
			this.tickAfterStomp = time + 1;
		}
//		else CharaPowerAct.LOGGER.info("Movement: {}->{}", oldPos, this.getPos());
	}

	@Inject(method = "readCustomDataFromNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
	private void readCPADataFromNbt(NbtCompound nbt, CallbackInfo ci) {
		super.readCustomDataFromNbt(nbt);

		if(nbt.contains(CPANbtKeys.DATA, NbtElement.COMPOUND_TYPE)) {
			NbtCompound persistentCPAData = nbt.getCompound(CPANbtKeys.DATA);

			boolean extraLogging = CharaPowerAct.CONFIG.logNBTReadWrite();
			if(extraLogging) CharaPowerAct.LOGGER.info("Reading player NBT:\nEnabled: {}\nCharacter: {}\nPower-up: {}",
					persistentCPAData.getBoolean(CPANbtKeys.ENABLED),
					persistentCPAData.getString(CPANbtKeys.CHARACTER),
					persistentCPAData.getString(CPANbtKeys.POWER_UP));

			if(persistentCPAData.getBoolean(CPANbtKeys.ENABLED)) {
				String storedCharacterID = persistentCPAData.getString(CPANbtKeys.CHARACTER);
				if(storedCharacterID.isEmpty()) {
					CharaPowerAct.LOGGER.error("Shocking error: A player's NBT data claims the mod is enabled, but no character ID is stored?!");
				}
				else if(RegistryManager.CHARACTERS.containsId(Identifier.of(storedCharacterID))) {
					ParsedCharacter storedCharacter = Objects.requireNonNull(RegistryManager.CHARACTERS.get(Identifier.of(storedCharacterID)));
					String storedPowerUpID = persistentCPAData.getString(CPANbtKeys.POWER_UP);
					if(storedPowerUpID.isEmpty()) {
						CharaPowerAct.LOGGER.error("Shocking error: A player's NBT data claims the mod is enabled, and a character ID is stored, but no power-up ID is stored?!");
					}
					else {
						if(!RegistryManager.POWER_UPS.containsId(Identifier.of(storedPowerUpID))) {
							CharaPowerAct.LOGGER.error("A player's NBT data contains an invalid Power-up ID: {}." +
									"The player will instead be set to their character's default power-up state.", storedPowerUpID);
							storedPowerUpID = storedCharacter.INITIAL_POWER_UP.ID.toString();
						}

						if(extraLogging)
							CharaPowerAct.LOGGER.info("Loaded a full set of CPA Data from NBT. This is {} in {} form.", storedCharacterID, storedPowerUpID);

						CPAServerPlayerData data = this.cpa$getCPAData();
						if(this.networkHandler == null) {
							if(extraLogging)
								CharaPowerAct.LOGGER.info("Player is not yet ready for networking. Assigning silently for later synchronization...");
							data.setupVariablesBeforeInitialApply(
									storedCharacter,
									RegistryManager.POWER_UPS.get(Identifier.of(storedPowerUpID))
							);
						}
						else {
							if(extraLogging) CharaPowerAct.LOGGER.info("Syncing data from NBT...");
							data.assignCharacter(storedCharacterID);
							data.assignPowerForm(storedPowerUpID);
						}
					}
				}
				else CharaPowerAct.LOGGER.error("A player's NBT data contains an invalid Character ID: {}", storedCharacterID);

//				CPAServerPlayerData data = cpa$getCPAData();
//				if(networkHandler != null) {
//					data.assignCharacter(getCharacterID(nbt));
//					data.assignPowerForm(getPowerFormID(nbt));
//				}
//				else data.preInitialApply(persistentCPAData.getBoolean("Enabled"), getPowerForm(nbt), getCharacter(nbt));
			}
		}
	}

	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player) {
		super.onStartedTrackingBy(player);
		CPADataPackets.syncCPADataToPlayerS2C((ServerPlayerEntity) (Object) this, player);
	}
}
