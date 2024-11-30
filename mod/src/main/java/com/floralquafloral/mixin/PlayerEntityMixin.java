package com.floralquafloral.mixin;

import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.moveable.MarioMoveableData;
import com.floralquafloral.registries.states.character.ParsedCharacter;
import com.floralquafloral.registries.states.powerup.ParsedPowerUp;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
	private PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Shadow public float strideDistance;

	@Shadow public float prevStrideDistance;

	@Override public void setSwimming(boolean swimming) {
		if(swimming && MarioDataManager.getMarioData(this).isEnabled()) return;
		super.setSwimming(swimming);
	}

	@Override
	public boolean isInSneakingPose() {
		return super.isInSneakingPose();
	}

	@Override
	public void setPose(EntityPose pose) {
		super.setPose(pose);
	}

	@Override
	protected void playStepSound(BlockPos pos, BlockState state) {
		super.playStepSound(pos, state);
	}

	@Override
	public void move(MovementType movementType, Vec3d movement) {
		super.move(movementType, movement);
	}

	@Override
	public boolean startRiding(Entity entity, boolean force) {
		boolean startedRiding = super.startRiding(entity, force);

		return startedRiding;
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
	}

	@Override
	public NbtCompound writeNbt(NbtCompound nbt) {
		return super.writeNbt(nbt);
	}

	@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
	private void travelHook(Vec3d movementInput, CallbackInfo ci) {
		MarioPlayerData data = MarioDataManager.getMarioData(this);
		if(data instanceof MarioMoveableData moveableData && data.useMarioPhysics()
				&& moveableData.travelHook(movementInput.z, movementInput.x))
			ci.cancel();
	}

	@Inject(method = "shouldSwimInFluids", at = @At("HEAD"), cancellable = true)
	private void preventVanillaJumpingSwimming(CallbackInfoReturnable<Boolean> cir) {
		if(MarioDataManager.getMarioData(this).useMarioPhysics())
			cir.setReturnValue(false);
	}

	@Inject(at = @At("TAIL"), method = "getBaseDimensions(Lnet/minecraft/entity/EntityPose;)Lnet/minecraft/entity/EntityDimensions;", cancellable = true)
	private void getBaseDimensions(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
		MarioPlayerData data = MarioDataManager.getMarioData(this);
		if(data.isEnabled()) {
			// Returns the standing hitbox if being used by Mario while he can't sneak
			if(data.isSneakProhibited() && pose == EntityPose.CROUCHING) {
				cir.setReturnValue(data.getMario().getBaseDimensions(EntityPose.STANDING));
				return;
			}

			ParsedPowerUp powerUp = data.getPowerUp();
			ParsedCharacter character = data.getCharacter();

			float widthFactor = powerUp.WIDTH_FACTOR * character.WIDTH_FACTOR;
			float heightFactor = powerUp.HEIGHT_FACTOR * character.HEIGHT_FACTOR;
			if(pose == EntityPose.CROUCHING) heightFactor *= 0.6F;

//			float eyeHeightOffset = 0;
//			if(data.isClient() && data.getMario().isMainPlayer() &&
//					BumpManager.eyeAdjustmentParticle != null && BumpManager.eyeAdjustmentParticle.isAlive()) {
//				eyeHeightOffset = BumpManager.eyeAdjustmentParticle.lastOffset;
//				MarioQuaMario.LOGGER.info("Found it!\n{}\n{}", BumpManager.eyeAdjustmentParticle, BumpManager.eyeAdjustmentParticle.lastOffset);
//			}

			EntityDimensions resultDimensions = cir.getReturnValue();

			cir.setReturnValue(new EntityDimensions(
					resultDimensions.width() * widthFactor,
					resultDimensions.height() * heightFactor,
					resultDimensions.eyeHeight() * heightFactor,
					resultDimensions.attachments(), resultDimensions.fixed()
			));
		}
	}

	@Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
	public void slideOffLedges(CallbackInfoReturnable<Boolean> cir) {
		MarioPlayerData data = MarioDataManager.getMarioData(this);
		if(data.useMarioPhysics() && data.getAction().SNEAK_LEGALITY.slipOffLedges()) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "tickMovement", at = @At("TAIL"))
	public void preventViewBobbing(CallbackInfo ci) {
		MarioPlayerData data = MarioDataManager.getMarioData(this);
		if(data.isClient() && !data.getAction().SLIDING_STATUS.doViewBobbing()) {
			strideDistance = prevStrideDistance * 0.6F;
		}
	}

	@Inject(method = "shouldDismount", at = @At("HEAD"), cancellable = true)
	public void changeDismounting(CallbackInfoReturnable<Boolean> cir) {
		MarioPlayerData data = MarioDataManager.getMarioData(this);
		if(data.isEnabled()) {
			cir.setReturnValue(data.attemptDismount);
			if(data.attemptDismount) data.attemptDismount = false;
		}
	}

	@WrapWithCondition(method = "jump", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;jump()V"))
	private boolean preventLivingEntityJump(LivingEntity instance) {
		return !MarioDataManager.getMarioData(this).useMarioPhysics();
	}


}
