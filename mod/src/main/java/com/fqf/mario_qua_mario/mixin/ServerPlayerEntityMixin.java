package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import com.fqf.mario_qua_mario.mariodata.injections.AdvMarioServerDataHolder;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedCharacter;
import com.fqf.mario_qua_mario.util.MarioNbtKeys;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements AdvMarioServerDataHolder {
	@Shadow public ServerPlayNetworkHandler networkHandler;

	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@WrapMethod(method = "damage")
	private boolean modifyIncomingDamage(DamageSource source, float amount, Operation<Boolean> original) {
		float modifiedAmount = this.mqm$getMarioData().getCharacter().modifyIncomingDamage(this.mqm$getMarioData(), source, amount);
		return modifiedAmount > 0 && original.call(source, modifiedAmount);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
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

//	@Unique
//	private static @NotNull <T extends ParsedMarioState> T getDataFromNbt(String ID, Identifier defaultID, Registry<T> registry) {
//		@Nullable T attempted = registry.get(Identifier.of(ID));
//		return attempted == null ? Objects.requireNonNull(registry.get(defaultID)) : attempted;
//	}
}
