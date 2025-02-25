package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import com.fqf.mario_qua_mario.mariodata.injections.AdvMarioServerDataHolder;
import com.fqf.mario_qua_mario.registries.ParsedMarioState;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedCharacter;
import com.fqf.mario_qua_mario.registries.power_granting.ParsedPowerUp;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registry;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.Objects;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements AdvMarioServerDataHolder {
	@Shadow public ServerPlayNetworkHandler networkHandler;

	public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound nbt) {
		super.readCustomDataFromNbt(nbt);

//		if(nbt.contains(MarioQuaMario.MOD_DATA_KEY, NbtElement.COMPOUND_TYPE)) {
//			NbtCompound persistentData = nbt.getCompound(MarioQuaMario.MOD_DATA_KEY);
//
//			MarioQuaMario.LOGGER.info("Reading player NBT:\nEnabled: {}\nPower-up: {}\nCharacter: {}",
//					persistentData.getBoolean("Enabled"),
//					persistentData.getString("PowerUp"),
//					persistentData.getString("Character"));
//
//			MarioServerPlayerData data = mqm$getMarioData();
//			if(networkHandler != null) {
////				data.setEnabled(persistentData.getBoolean("Enabled"));
//				data.assignCharacter(getCharacterID(nbt));
//				data.assignPowerUp(getPowerUpID(nbt));
//			}
//			else data.preInitialApply(persistentData.getBoolean("Enabled"), getPowerUp(nbt), getCharacter(nbt));
//		}
	}

	@Unique
	private static ParsedCharacter getCharacter(NbtCompound nbt) {
		return getDataFromNbt(getCharacterID(nbt), MarioQuaMario.makeID("mario"), RegistryManager.CHARACTERS);
	}
	@Unique
	private static ParsedPowerUp getPowerUp(NbtCompound nbt) {
		return getDataFromNbt(getPowerUpID(nbt), getCharacter(nbt).INITIAL_POWER_UP.ID, RegistryManager.POWER_UPS);
	}
	@Unique
	private static String getCharacterID(NbtCompound nbt) {
		return nbt.getString("Characters");
	}
	@Unique
	private static String getPowerUpID(NbtCompound nbt) {
		return nbt.getString("PowerUp");
	}

	@Unique
	private static @NotNull <T extends ParsedMarioState> T getDataFromNbt(String ID, Identifier defaultID, Registry<T> registry) {
		@Nullable T attempted = registry.get(Identifier.of(ID));
		return attempted == null ? Objects.requireNonNull(registry.get(defaultID)) : attempted;
	}
}
