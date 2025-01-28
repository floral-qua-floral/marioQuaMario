package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.Arrangement;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.LimbAnimation;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario.mariodata.MarioAnimationData;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.mariodata.util.ArrangementSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin<T extends LivingEntity> extends BipedEntityModel<T> {
	@Shadow @Final private ModelPart cloak;

	public PlayerEntityModelMixin(ModelPart root) {
		super(root);
		throw new AssertionError("Calling constructor on mixin?!");
	}

	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
	private void resetBodyPartsToDefaultPose(T livingEntity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
		this.head.roll = 0;
	}

	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", shift = At.Shift.AFTER))
	private void animateMario(T livingEntity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
		AbstractClientPlayerEntity mario = (AbstractClientPlayerEntity) livingEntity;
		mario.mqm$getAnimationData().setAngles(
				MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true), mario,
				this.head, this.body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg,
				this.rightArmPose, this.leftArmPose
		);
	}

	@Unique
	private static void lerpBetweenArrangements(ModelPart part, Arrangement prevTick, Arrangement thisTick, float tickDelta, Arrangement storeDelta) {
		float newX = MathHelper.lerp(tickDelta, prevTick.x, thisTick.x);
		float newY = MathHelper.lerp(tickDelta, prevTick.y, thisTick.y);
		float newZ = MathHelper.lerp(tickDelta, prevTick.z, thisTick.z);
		storeDelta.setPos(newX - part.pivotX, newY - part.pivotY, newZ - part.pivotZ);
		part.setPivot(newX, newY, newZ);

		float newPitch = MarioAnimationData.lerpRadians(tickDelta, prevTick.pitch, thisTick.pitch);
		float newYaw = MarioAnimationData.lerpRadians(tickDelta, prevTick.yaw, thisTick.yaw);
		float newRoll = MarioAnimationData.lerpRadians(tickDelta, prevTick.roll, thisTick.roll);
		storeDelta.setAngles(newPitch - part.pitch, newYaw - part.yaw, newRoll - part.roll);
		part.setAngles(newPitch, newYaw, newRoll);
		part.pitch = newPitch;
	}

	@Unique
	private void intelligentlyAnimateArm(MarioPlayerData data, MarioAnimationData animData, ModelPart part, Arrangement arrangement, LimbAnimation animation, float progress, boolean isMirrored, boolean isRight) {
		ArmPose thisArmPose = isRight ? this.rightArmPose : this.leftArmPose;
		ArmPose otherArmPose = isRight ? this.leftArmPose : this.rightArmPose;
		if(MarioAnimationData.isArmBusy(thisArmPose, otherArmPose)) {
//			MarioAnimationData.setupArrangement(part, arrangement);
			arrangement.addPos(animData.thisTickArrangements.BODY.x, animData.thisTickArrangements.BODY.y, animData.thisTickArrangements.BODY.z);
		}
		else MarioAnimationData.animatePart(data, part, arrangement, animation, progress, isMirrored);
	}

	@Unique
	private void undoLastFrame(MarioAnimationData animationData, float multiplier) {
		MarioAnimationData.undoFrame(this.head, animationData.prevFrameAnimationDeltas.HEAD, multiplier);
		MarioAnimationData.undoFrame(this.body, animationData.prevFrameAnimationDeltas.BODY, multiplier);

		MarioAnimationData.undoFrame(this.rightArm, animationData.prevFrameAnimationDeltas.RIGHT_ARM, multiplier);
		MarioAnimationData.undoFrame(this.leftArm, animationData.prevFrameAnimationDeltas.LEFT_ARM, multiplier);

		MarioAnimationData.undoFrame(this.rightLeg, animationData.prevFrameAnimationDeltas.RIGHT_LEG, multiplier);
		MarioAnimationData.undoFrame(this.leftLeg, animationData.prevFrameAnimationDeltas.LEFT_LEG, multiplier);
	}
}
