package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import com.fqf.mario_qua_mario.mariodata.injections.AdvMarioServerDataHolder;
import com.fqf.mario_qua_mario.packets.MarioDataPackets;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedCharacter;
import com.fqf.mario_qua_mario.util.MarioNbtKeys;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
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
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements AdvMarioServerDataHolder {
	@Shadow public ServerPlayNetworkHandler networkHandler;

	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Unique private long tickAfterStomp;

	@Override
	public void move(MovementType movementType, Vec3d movement) {
		MarioServerPlayerData data = this.mqm$getMarioData();
		Vec3d oldMovement = movement;
		Vec3d oldPos = this.getPos();
		// Only perform stomp checks on movement that comes from a player packet (as opposed to server-side travel).
		// Should this change??
		long time = this.getWorld().getTime();
		if(data.isEnabled() && data.doMarioTravel() && data.getAction().STOMP_TYPE != null
				&& (movementType == MovementType.PLAYER || movementType == MovementType.SELF)
				&& time != this.tickAfterStomp && time != this.tickAfterStomp - 1)
			movement = data.getAction().STOMP_TYPE.moveHook(data, movement);

		super.move(movementType, movement);
		if(!oldMovement.equals(movement)) {
//			MarioQuaMario.LOGGER.info("Server-sided stomp after move completion:\nTick: {}\tForbiddenTick: {}\tMovetype: {}\nOld position: {}\nMovement: {}\nNew position: {}\nDifference: {}",
//					this.getWorld().getTime(), this.tickAfterStomp, movementType, oldPos, movement, this.getPos(), this.getPos().subtract(oldPos));
			this.tickAfterStomp = time + 1;
		}
//		else MarioQuaMario.LOGGER.info("Movement: {}->{}", oldPos, this.getPos());
	}

	@Inject(method = "readCustomDataFromNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V", shift = At.Shift.AFTER))
	private void readMarioDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
		super.readCustomDataFromNbt(nbt);

		if(nbt.contains(MarioNbtKeys.DATA, NbtElement.COMPOUND_TYPE)) {
			NbtCompound persistentMarioData = nbt.getCompound(MarioNbtKeys.DATA);

			MarioQuaMario.LOGGER.info("Reading player NBT:\nEnabled: {}\nCharacter: {}\nPower-up: {}",
					persistentMarioData.getBoolean(MarioNbtKeys.ENABLED),
					persistentMarioData.getString(MarioNbtKeys.CHARACTER),
					persistentMarioData.getString(MarioNbtKeys.POWER_UP));

			if(persistentMarioData.getBoolean(MarioNbtKeys.ENABLED)) {
				String storedCharacterID = persistentMarioData.getString(MarioNbtKeys.CHARACTER);
				if(storedCharacterID.isEmpty()) {
					MarioQuaMario.LOGGER.error("Shocking error: A player's NBT data claims the mod is enabled, but no character ID is stored?!");
				}
				else if(RegistryManager.CHARACTERS.containsId(Identifier.of(storedCharacterID))) {
					ParsedCharacter storedCharacter = Objects.requireNonNull(RegistryManager.CHARACTERS.get(Identifier.of(storedCharacterID)));
					String storedPowerUpID = persistentMarioData.getString(MarioNbtKeys.POWER_UP);
					if(storedPowerUpID.isEmpty()) {
						MarioQuaMario.LOGGER.error("Shocking error: A player's NBT data claims the mod is enabled, and a character ID is stored, but no power-up ID is stored?!");
					}
					else {
						if(!RegistryManager.POWER_UPS.containsId(Identifier.of(storedPowerUpID))) {
							MarioQuaMario.LOGGER.error("A player's NBT data contains an invalid Power-up ID: {}." +
									"The player will instead be set to their character's default power-up state.", storedPowerUpID);
							storedPowerUpID = storedCharacter.INITIAL_POWER_UP.ID.toString();
						}

						MarioQuaMario.LOGGER.info("Loaded a full set of Mario Data from NBT. This is {} in {} form.", storedCharacterID, storedPowerUpID);
						MarioServerPlayerData data = this.mqm$getMarioData();
						if(this.networkHandler == null) {
							MarioQuaMario.LOGGER.info("Player is not yet ready for networking. Assigning silently for later synchronization...");
							data.setupVariablesBeforeInitialApply(
									RegistryManager.CHARACTERS.get(Identifier.of(storedCharacterID)),
									RegistryManager.POWER_UPS.get(Identifier.of(storedPowerUpID))
							);
						}
						else {
							MarioQuaMario.LOGGER.info("Syncing data from NBT...");
							data.assignCharacter(storedCharacterID);
							data.assignPowerUp(storedPowerUpID);
						}
					}
				}
				else MarioQuaMario.LOGGER.error("A player's NBT data contains an invalid Character ID: {}", storedCharacterID);

//				MarioServerPlayerData data = mqm$getMarioData();
//				if(networkHandler != null) {
//					data.assignCharacter(getCharacterID(nbt));
//					data.assignPowerUp(getPowerUpID(nbt));
//				}
//				else data.preInitialApply(persistentMarioData.getBoolean("Enabled"), getPowerUp(nbt), getCharacter(nbt));
			}
		}
	}

	@Override
	public void onStartedTrackingBy(ServerPlayerEntity player) {
		super.onStartedTrackingBy(player);
		MarioDataPackets.syncMarioDataToPlayerS2C((ServerPlayerEntity) (Object) this, player);
	}

	@Inject(method = "requestTeleport", at = @At("HEAD"))
	private void teleportHook(double destX, double destY, double destZ, CallbackInfo ci) {
		MarioQuaMario.LOGGER.info("requestTeleport occurred!!!");
	}

	//	@Unique
//	private static @NotNull <T extends ParsedMarioState> T getDataFromNbt(String ID, Identifier defaultID, Registry<T> registry) {
//		@Nullable T attempted = registry.get(Identifier.of(ID));
//		return attempted == null ? Objects.requireNonNull(registry.get(defaultID)) : attempted;
//	}
}
