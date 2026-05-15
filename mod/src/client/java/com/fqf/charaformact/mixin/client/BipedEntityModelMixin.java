package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact_api.definitions.states.actions.util.animation.LimbAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.AnimalModel;
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
public abstract class BipedEntityModelMixin<T extends LivingEntity> extends AnimalModel<T> {
	@Shadow @Final public ModelPart rightArm;
	@Shadow @Final public ModelPart leftArm;
	@Shadow @Final public ModelPart rightLeg;
	@Shadow @Final public ModelPart leftLeg;

//	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "arm"))

	@Shadow @Final public ModelPart head;

	@Shadow @Final public ModelPart body;

	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
	private void storeLivingEntityReference(
			T entity,
			float limbAngle, float limbDistance,
			float animationProgress,
			float headYaw, float headPitch,
			CallbackInfo ci,
			@Share("apply") LocalBooleanRef applyRef,
			@Share("player") LocalRef<AbstractClientPlayerEntity> playerRef,
			@Share("rightArm") LocalBooleanRef rightArmRef,
			@Share("leftArm") LocalBooleanRef leftArmRef,
			@Share("rightLeg") LocalBooleanRef rightLegRef,
			@Share("leftLeg") LocalBooleanRef leftLegRef
			) {
		if(entity instanceof AbstractClientPlayerEntity player) {
			PlayermodelAnimation animation = player.cfa$getAnimationData().currentAnim;
			if(animation != null) {
				applyRef.set(true);
				playerRef.set(player);

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
			@Share("player") LocalRef<AbstractClientPlayerEntity> playerRef,
			@Share("rightArm") LocalBooleanRef rightArmRef,
			@Share("leftArm") LocalBooleanRef leftArmRef,
			@Share("rightLeg") LocalBooleanRef rightLegRef,
			@Share("leftLeg") LocalBooleanRef leftLegRef
	) {
		if(applyRef.get()) {
			if(instance == this.head) {
				newValue = playerRef.get().cfa$getAnimationData().counterRotateHead(this.head, newValue);
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

	@ModifyExpressionValue(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;sneaking:Z"))
	private boolean doNotApplySneaking(boolean original, @Share("apply") LocalBooleanRef applyRef) {
		if(applyRef.get()) return false;
		return original;
	}

	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/model/ModelPart;pivotX:F", ordinal = 1, shift = At.Shift.AFTER))
	private void resetArmPivotX(
			T entity,
			float limbAngle, float limbDistance,
			float animationProgress,
			float headYaw, float headPitch,
			CallbackInfo ci
	) {
		if(entity instanceof AbstractClientPlayerEntity player && player.cfa$getCfaData().isEnabled()) {
			ModelTransform armDefaultTransform = this.rightArm.getDefaultTransform();
			this.rightArm.pivotZ = armDefaultTransform.pivotZ;
			this.rightArm.pivotX = armDefaultTransform.pivotX;
			armDefaultTransform = this.leftArm.getDefaultTransform();
			this.leftArm.pivotZ = armDefaultTransform.pivotZ;
			this.leftArm.pivotX = armDefaultTransform.pivotX;
		}
	}

	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;SPYGLASS:Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;", ordinal = 0))
	private void resetModelPartsToActualDefaults(
			T entity,
			float limbAngle, float limbDistance,
			float animationProgress,
			float headYaw, float headPitch,
			CallbackInfo ci
	) {
		if(entity instanceof AbstractClientPlayerEntity player && player.cfa$getCfaData().isEnabled()) {
			this.body.pitch = this.body.getDefaultTransform().pitch;
			ModelTransform legDefaultTransform = this.rightLeg.getDefaultTransform();
			this.rightLeg.pivotY = legDefaultTransform.pivotY;
			this.rightLeg.pivotZ = legDefaultTransform.pivotZ;
			legDefaultTransform = this.leftLeg.getDefaultTransform();
			this.leftLeg.pivotY = legDefaultTransform.pivotY;
			this.leftLeg.pivotZ = legDefaultTransform.pivotZ;
			this.head.pivotY = this.head.getDefaultTransform().pivotY;
			this.body.pivotY = this.body.getDefaultTransform().pivotY;
			this.leftArm.pivotY = this.leftArm.getDefaultTransform().pivotY;
			this.rightArm.pivotY = this.leftArm.getDefaultTransform().pivotY;
		}
	}

	@Inject(method = "animateArms", at = @At("RETURN"))
	private void useActualDefaultsForArmPositions(T entity, float animationProgress, CallbackInfo ci) {
		if(entity instanceof AbstractClientPlayerEntity player && player.cfa$getCfaData().isEnabled() && !(this.handSwingProgress <= 0.0F)) {
			ModelTransform armDefaultTransform = this.rightArm.getDefaultTransform();
			this.rightArm.pivotX = MathHelper.cos(this.body.yaw) * armDefaultTransform.pivotX;
			this.rightArm.pivotZ = -MathHelper.sin(this.body.yaw) * armDefaultTransform.pivotX;
			armDefaultTransform = this.leftArm.getDefaultTransform();
			this.leftArm.pivotX = MathHelper.cos(this.body.yaw) * armDefaultTransform.pivotX;
			this.leftArm.pivotZ = -MathHelper.sin(this.body.yaw) * armDefaultTransform.pivotX;
		}
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
