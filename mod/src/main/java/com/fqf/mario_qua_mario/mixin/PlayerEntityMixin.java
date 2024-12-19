package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.mariodata.injections.MarioDataHolder;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity implements MarioDataHolder {
	private PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "tick", at = @At("HEAD"))
	private void tickHook(CallbackInfo ci) {
		this.mqm$getMarioData().tick();
	}

	@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
	private void travelHook(Vec3d movementInput, CallbackInfo ci) {
		if(this.mqm$getMarioData() instanceof MarioMoveableData moveableData
				&& moveableData.travelHook(movementInput.z, movementInput.x))
			ci.cancel();
	}

	@Override
	protected boolean stepOnBlock(BlockPos pos, BlockState state, boolean playSound, boolean emitEvent, Vec3d movement) {
		return switch (this.mqm$getMarioData().getAction().SLIDING_STATUS) {
			case SLIDING, SLIDING_SILENT, SKIDDING -> false;
			default -> super.stepOnBlock(pos, state, playSound, emitEvent, movement);
		};
	}

	@Override
	protected void playStepSounds(BlockPos pos, BlockState state) {
		switch (this.mqm$getMarioData().getAction().SLIDING_STATUS) {
			case SLIDING, SLIDING_SILENT, SKIDDING -> {}
			default -> super.playStepSounds(pos, state);
		};
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound nbt) {
		super.writeCustomDataToNbt(nbt);

		NbtCompound persistentData = new NbtCompound();
		MarioPlayerData data = mqm$getMarioData();

		persistentData.putBoolean("Enabled", data.isEnabled());
		persistentData.putString("PowerUp", data.getPowerUpID().toString());
		persistentData.putString("Character", data.getCharacterID().toString());

		MarioQuaMario.LOGGER.info("Wrote player NBT:\nEnabled: {}\nPower-up: {}\nCharacter: {}",
				persistentData.getBoolean("Enabled"),
				persistentData.getString("PowerUp"),
				persistentData.getString("Character"));

		nbt.put(MarioQuaMario.MOD_DATA_KEY, persistentData);
	}
}
