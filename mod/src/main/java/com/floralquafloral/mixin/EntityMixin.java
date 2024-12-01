package com.floralquafloral.mixin;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.registries.states.action.ParsedAction;
import com.floralquafloral.registries.states.character.ParsedCharacter;
import com.floralquafloral.registries.states.powerup.ParsedPowerUp;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockCollisionSpliterator;
import net.minecraft.world.World;
import org.joml.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

@Mixin(Entity.class)
public class EntityMixin {
	@Inject(method = "playStepSounds", at = @At("HEAD"), cancellable = true)
	private void preventStepSounds(BlockPos pos, BlockState state, CallbackInfo ci) {
		if(((Entity) (Object) this) instanceof PlayerEntity player) {
			MarioPlayerData data = MarioDataManager.getMarioData(player);
			if(!data.getAction().SLIDING_STATUS.doFootsteps())
				ci.cancel();
		}
	}

	@Unique private final String MOD_DATA_NAME = MarioQuaMario.MOD_ID + ".data";

	@Inject(method = "writeNbt", at = @At("HEAD"))
	protected void writeMarioData(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
		if((Entity) (Object) this instanceof ServerPlayerEntity player) {
			NbtCompound persistentMarioData = new NbtCompound();
			MarioPlayerData data = MarioDataManager.getMarioData(player);

			persistentMarioData.putBoolean("Enabled", data.isEnabled());
			persistentMarioData.putString("Character", data.getCharacterID().toString());
			persistentMarioData.putString("PowerUp", data.getPowerUpID().toString());


			MarioQuaMario.LOGGER.info("Writing player NBT"
					+ "\nEnabled: " + persistentMarioData.getBoolean("Enabled")
					+ "\nCharacter: " + persistentMarioData.getString("Character")
					+ "\nCharacterID: " + Identifier.of(persistentMarioData.getString("Character"))
					+ "\nParsedCharacter: " + RegistryManager.CHARACTERS.get(Identifier.of(persistentMarioData.getString("Character")))
			);

			nbt.put(MOD_DATA_NAME, persistentMarioData);
		}
	}

	@Inject(method = "readNbt", at = @At("HEAD"))
	protected void readMarioData(NbtCompound nbt, CallbackInfo ci) {
		if((Entity) (Object) this instanceof ServerPlayerEntity player) {
			MarioQuaMario.LOGGER.info("Reading player NBT!!!"
					+ "\nContains?: " + nbt.contains(MOD_DATA_NAME, NbtElement.COMPOUND_TYPE)
			);
			if(nbt.contains(MOD_DATA_NAME, NbtElement.COMPOUND_TYPE)) {
				NbtCompound persistentMarioData = nbt.getCompound(MOD_DATA_NAME);
				MarioQuaMario.LOGGER.info("Reading player NBT 2"
						+ "\nEnabled: " + persistentMarioData.getBoolean("Enabled")
						+ "\nCharacter: " + persistentMarioData.getString("Character")
						+ "\nCharacterID: " + Identifier.of(persistentMarioData.getString("Character"))
						+ "\nParsedCharacter: " + RegistryManager.CHARACTERS.get(Identifier.of(persistentMarioData.getString("Character")))
				);

				MarioServerData data = (MarioServerData) MarioDataManager.getMarioData(player);
//				if(data.getMario().networkHandler != null) {
//					data.setEnabled(persistentMarioData.getBoolean("Enabled"));
//					data.setPowerUp(persistentMarioData.getString("PowerUp"));
//					data.setCharacter(persistentMarioData.getString("Character"));
//				}

				data.setEnabledInternal(persistentMarioData.getBoolean("Enabled"));
				ParsedCharacter character = RegistryManager.CHARACTERS.get(Identifier.of(persistentMarioData.getString("Character")));
				if(character != null) data.setCharacter(character);
				else data.setCharacter(data.getCharacter());

				ParsedPowerUp powerUp = RegistryManager.POWER_UPS.get(Identifier.of(persistentMarioData.getString("PowerUp")));
				if(powerUp != null) data.setPowerUp(powerUp);
				else data.setPowerUp(data.getPowerUp());
			}
			else {

			}
		}
	}
}
