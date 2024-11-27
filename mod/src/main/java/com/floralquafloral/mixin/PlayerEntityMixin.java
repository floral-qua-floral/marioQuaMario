package com.floralquafloral.mixin;

import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.moveable.MarioMoveableData;
import com.floralquafloral.registries.states.character.ParsedCharacter;
import com.floralquafloral.registries.states.powerup.ParsedPowerUp;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
	@Shadow public float strideDistance;

	@Shadow public float prevStrideDistance;

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
