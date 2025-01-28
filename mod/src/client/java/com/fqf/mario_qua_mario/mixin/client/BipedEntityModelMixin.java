package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.LimbAnimation;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityModel.class)
public class BipedEntityModelMixin<T extends LivingEntity> {
	@Shadow @Final public ModelPart rightArm;
	@Shadow @Final public ModelPart leftArm;
	@Shadow @Final public ModelPart rightLeg;
	@Shadow @Final public ModelPart leftLeg;

//	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "arm"))

	@Shadow @Final public ModelPart head;

	@Shadow @Final public ModelPart body;

	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
	private void storeLivingEntityReference(
			T livingEntity,
			float f, float g, float h, float i, float j, CallbackInfo ci,
			@Share("apply") LocalBooleanRef applyRef,
			@Share("mario") LocalRef<AbstractClientPlayerEntity> marioRef,
			@Share("rightArm") LocalBooleanRef rightArmRef,
			@Share("leftArm") LocalBooleanRef leftArmRef,
			@Share("rightLeg") LocalBooleanRef rightLegRef,
			@Share("leftLeg") LocalBooleanRef leftLegRef
			) {
		if(livingEntity instanceof AbstractClientPlayerEntity mario) {
			PlayermodelAnimation animation = mario.mqm$getMarioData().getAction().ANIMATION;
			if(animation != null) {
				applyRef.set(true);
				marioRef.set(mario);

				rightArmRef.set(animationSuppressesSwinging(animation.rightArmAnimation()));
				leftArmRef.set(animationSuppressesSwinging(animation.leftArmAnimation()));
				rightLegRef.set(animationSuppressesSwinging(animation.rightLegAnimation()));
				leftLegRef.set(animationSuppressesSwinging(animation.leftLegAnimation()));

				return;
			}
		}
		applyRef.set(false);
	}

	@WrapOperation(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPart;pitch:F", opcode = Opcodes.PUTFIELD))
	private void preventLimbSwinging(
			ModelPart instance, float newValue, Operation<Void> original,
			@Share("apply") LocalBooleanRef applyRef,
			@Share("mario") LocalRef<AbstractClientPlayerEntity> marioRef,
			@Share("rightArm") LocalBooleanRef rightArmRef,
			@Share("leftArm") LocalBooleanRef leftArmRef,
			@Share("rightLeg") LocalBooleanRef rightLegRef,
			@Share("leftLeg") LocalBooleanRef leftLegRef
	) {
		if(applyRef.get()) {
			if(instance == this.head) {
				float modelPitchAdjustment = MathHelper.RADIANS_PER_DEGREE * MathHelper.wrapDegrees(MathHelper.DEGREES_PER_RADIAN * marioRef.get().mqm$getAnimationData().headPitchOffset);
				float modelYawAdjustment = MathHelper.RADIANS_PER_DEGREE * MathHelper.wrapDegrees(MathHelper.DEGREES_PER_RADIAN * marioRef.get().mqm$getAnimationData().headYawOffset);
				float maxYawAdjustment = 60F * MathHelper.RADIANS_PER_DEGREE;
				newValue = MathHelper.clamp(newValue + modelPitchAdjustment, -MathHelper.HALF_PI, MathHelper.HALF_PI * 0.9F);
				this.head.yaw = MathHelper.clamp(this.head.yaw + modelYawAdjustment, this.body.yaw - maxYawAdjustment, this.body.yaw + maxYawAdjustment);
			}
			else if(
					attemptSuppression(rightArmRef, instance, this.rightArm)
					|| attemptSuppression(leftArmRef, instance, this.leftArm)
					|| attemptSuppression(rightLegRef, instance, this.rightLeg)
					|| attemptSuppression(leftLegRef, instance, this.leftLeg)
			)
				newValue = 0;
		}
		original.call(instance, newValue);
	}

	@Unique
	private static boolean animationSuppressesSwinging(LimbAnimation animation) {
		return animation != null && !animation.shouldSwingWithMovement();
	}

	@Unique
	private static boolean attemptSuppression(LocalBooleanRef ref, ModelPart instance, ModelPart checkAgainstPart) {
		if(ref.get() && instance == checkAgainstPart) {
			ref.set(false);
			return true;
		}
		return false;
	}
}
