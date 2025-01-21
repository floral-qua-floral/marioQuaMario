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

//	@Unique public @NotNull ArrangementSet prevTickArrangements; // used for per-frame interpolation
//	@Unique public @NotNull ArrangementSet thisTickArrangements; // used for per-frame interpolation
//	@Unique public @NotNull ArrangementSet prevFrameAnimationDeltas; // used to undo the previous frame's animation at the start of the next frame (for mod compatibility)
//	@Unique private boolean animatedLastFrame;
//	@Unique private int animationTicks;
//
//	@Inject(method = "<init>", at = @At("TAIL"))
//	private void constructorHook(ModelPart root, boolean thinArms, CallbackInfo ci) {
//		this.prevTickArrangements = new ArrangementSet();
//		this.thisTickArrangements = new ArrangementSet();
//		this.prevFrameAnimationDeltas = new ArrangementSet();
//		this.animatedLastFrame = false;
//	}

	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("HEAD"))
	private void undoPreviousFrameAnimations(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
		AbstractClientPlayerEntity mario = (AbstractClientPlayerEntity) livingEntity;
		MarioPlayerData data = mario.mqm$getMarioData();
		MarioAnimationData animData = mario.mqm$getAnimationData();
		if(!data.isEnabled()) return;

		if(animData.animatedLastFrame) {
			animData.animatedLastFrame = false;
			if (data.resetAnimation) {
				MarioAnimationData.setupArrangement(this.head, animData.prevTickArrangements.HEAD);
				MarioAnimationData.setupArrangement(this.body, animData.prevTickArrangements.BODY);
				MarioAnimationData.setupArrangement(this.rightArm, animData.prevTickArrangements.RIGHT_ARM);
				MarioAnimationData.setupArrangement(this.leftArm, animData.prevTickArrangements.LEFT_ARM);
				MarioAnimationData.setupArrangement(this.rightLeg, animData.prevTickArrangements.RIGHT_LEG);
				MarioAnimationData.setupArrangement(this.leftLeg, animData.prevTickArrangements.LEFT_LEG);
				MarioAnimationData.setupArrangement(this.cloak, animData.prevTickArrangements.CAPE);
			}
			this.undoLastFrame(animData, -1);
		}
	}

	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", shift = At.Shift.AFTER))
	private void setAnglesHook(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
		AbstractClientPlayerEntity mario = (AbstractClientPlayerEntity) livingEntity;
		MarioPlayerData data = mario.mqm$getMarioData();
		MarioAnimationData animData = mario.mqm$getAnimationData();
		PlayermodelAnimation animation = data.getAction().ANIMATION;

		if(!data.isEnabled()) return;

		if(animation == null) {
			boolean apply = false;
			if(data.resetAnimation) {
				data.resetAnimation = false;
				animData.trailingFrame = data.prevAnimation != null;

				data.tickAnimation = false;
				if(animData.trailingFrame) {
					animData.thisTickArrangements.EVERYTHING.setPos(0, 0,0 );
					animData.thisTickArrangements.EVERYTHING.setAngles(0, 0,0 );
					animData.prevTickArrangements.EVERYTHING.setPos(
							animData.prevFrameAnimationDeltas.EVERYTHING.x,
							animData.prevFrameAnimationDeltas.EVERYTHING.y,
							animData.prevFrameAnimationDeltas.EVERYTHING.z
					);
					animData.prevTickArrangements.EVERYTHING.setAngles(
							animData.prevFrameAnimationDeltas.EVERYTHING.pitch,
							animData.prevFrameAnimationDeltas.EVERYTHING.yaw,
							animData.prevFrameAnimationDeltas.EVERYTHING.roll
					);
				}
			}

			if(animData.trailingFrame) {
				if(data.tickAnimation) {
					animData.trailingFrame = false;
					return;
				}
				else {
					// This has to occur every frame to ensure that the lerp will catch up with the limbs' current position
					MarioAnimationData.setupArrangement(this.head, animData.thisTickArrangements.HEAD);
					MarioAnimationData.setupArrangement(this.body, animData.thisTickArrangements.BODY);
					MarioAnimationData.setupArrangement(this.rightArm, animData.thisTickArrangements.RIGHT_ARM);
					MarioAnimationData.setupArrangement(this.leftArm, animData.thisTickArrangements.LEFT_ARM);
					MarioAnimationData.setupArrangement(this.rightLeg, animData.thisTickArrangements.RIGHT_LEG);
					MarioAnimationData.setupArrangement(this.leftLeg, animData.thisTickArrangements.LEFT_LEG);
					MarioAnimationData.setupArrangement(this.cloak, animData.thisTickArrangements.CAPE);
				}
			}
			else return;
		}
		else {
			if (data.tickAnimation) {
				data.tickAnimation = false;

				animData.prevTickArrangements = animData.thisTickArrangements;
				animData.thisTickArrangements = new ArrangementSet();

				if (data.resetAnimation) {
					data.resetAnimation = false;

					boolean shouldReset;
					boolean matches = animation.equals(data.prevAnimation);

					if(animation.progressHandler() == null) shouldReset = !matches;
					else if(animation.progressHandler().resetter() == null) shouldReset = !matches;
					else if(data.prevAnimation == null) shouldReset = true;
					else if(data.prevAnimation.progressHandler() == null) shouldReset = !matches;
					else if(data.prevAnimation.progressHandler().animationID() == null) shouldReset = !matches;
					else shouldReset = animation.progressHandler().resetter().shouldReset(data, data.prevAnimation.progressHandler().animationID());

					if(shouldReset) {
						animData.animationTicks = 0;

						animData.animationMirrored = animation.mirroringEvaluator() != null &&
								animation.mirroringEvaluator().shouldMirror(data,
										MarioAnimationData.isArmBusy(this.rightArmPose, this.leftArmPose), MarioAnimationData.isArmBusy(this.leftArmPose, this.rightArmPose),
										this.head.yaw - this.body.yaw);

						if (data.prevAnimation == null) {
							animData.prevTickArrangements.EVERYTHING.setPos(0, 0, 0);
							animData.prevTickArrangements.EVERYTHING.setAngles(0, 0, 0);
							MarioAnimationData.setupArrangement(this.head, animData.prevTickArrangements.HEAD);
							MarioAnimationData.setupArrangement(this.body, animData.prevTickArrangements.BODY);
							MarioAnimationData.setupArrangement(this.rightArm, animData.prevTickArrangements.RIGHT_ARM);
							MarioAnimationData.setupArrangement(this.leftArm, animData.prevTickArrangements.LEFT_ARM);
							MarioAnimationData.setupArrangement(this.rightLeg, animData.prevTickArrangements.RIGHT_LEG);
							MarioAnimationData.setupArrangement(this.leftLeg, animData.prevTickArrangements.LEFT_LEG);
							MarioAnimationData.setupArrangement(this.cloak, animData.prevTickArrangements.CAPE);
						}
					}
				}

				float progress;
				if(animation.progressHandler() == null) progress = 1;
				else progress = animation.progressHandler().calculator().calculateProgress(data, animData.animationTicks);
				animData.animationTicks++;

				boolean isMirrored = animData.animationMirrored;

				if (animation.entireBodyAnimation() != null) {
					animData.thisTickArrangements.EVERYTHING.setPos(0, 0, 0);
					animData.thisTickArrangements.EVERYTHING.setAngles(0, 0, 0);
					MarioAnimationData.applyMutator(data, animData.thisTickArrangements.EVERYTHING, animation.entireBodyAnimation().mutator(), progress, isMirrored);
				}

				MarioAnimationData.animatePart(data, this.head, animData.thisTickArrangements.HEAD, animation.headAnimation(), progress, isMirrored);
				MarioAnimationData.animatePart(data, this.body, animData.thisTickArrangements.BODY, animation.torsoAnimation(), progress, isMirrored);

				intelligentlyAnimateArm(
						data, animData,
						this.rightArm,
						animData.thisTickArrangements.RIGHT_ARM,
						isMirrored ? animation.leftArmAnimation() : animation.rightArmAnimation(),
						progress, isMirrored, true
				);
				intelligentlyAnimateArm(
						data, animData,
						this.leftArm,
						animData.thisTickArrangements.LEFT_ARM,
						isMirrored ? animation.rightArmAnimation() : animation.leftArmAnimation(),
						progress, isMirrored, false);

				MarioAnimationData.animatePart(
						data,
						this.rightLeg,
						animData.thisTickArrangements.RIGHT_LEG,
						isMirrored ? animation.leftLegAnimation() : animation.rightLegAnimation(),
						progress, isMirrored
				);
				MarioAnimationData.animatePart(
						data,
						this.leftLeg,
						animData.thisTickArrangements.LEFT_LEG,
						isMirrored ? animation.rightLegAnimation() : animation.leftLegAnimation(),
						progress, isMirrored
				);
			}
		}

		animData.animatedLastFrame = true;
		float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);

		lerpBetweenArrangements(this.head, animData.prevTickArrangements.HEAD, animData.thisTickArrangements.HEAD, tickDelta, animData.prevFrameAnimationDeltas.HEAD);
		lerpBetweenArrangements(this.body, animData.prevTickArrangements.BODY, animData.thisTickArrangements.BODY, tickDelta, animData.prevFrameAnimationDeltas.BODY);

		lerpBetweenArrangements(this.rightArm, animData.prevTickArrangements.RIGHT_ARM, animData.thisTickArrangements.RIGHT_ARM, tickDelta, animData.prevFrameAnimationDeltas.RIGHT_ARM);
		lerpBetweenArrangements(this.leftArm, animData.prevTickArrangements.LEFT_ARM, animData.thisTickArrangements.LEFT_ARM, tickDelta, animData.prevFrameAnimationDeltas.LEFT_ARM);

		lerpBetweenArrangements(this.rightLeg, animData.prevTickArrangements.RIGHT_LEG, animData.thisTickArrangements.RIGHT_LEG, tickDelta, animData.prevFrameAnimationDeltas.RIGHT_LEG);
		lerpBetweenArrangements(this.leftLeg, animData.prevTickArrangements.LEFT_LEG, animData.thisTickArrangements.LEFT_LEG, tickDelta, animData.prevFrameAnimationDeltas.LEFT_LEG);

		animData.prevFrameAnimationDeltas.EVERYTHING.setPos(
				MathHelper.lerp(tickDelta, animData.prevTickArrangements.EVERYTHING.x, animData.thisTickArrangements.EVERYTHING.x),
				MathHelper.lerp(tickDelta, animData.prevTickArrangements.EVERYTHING.y, animData.thisTickArrangements.EVERYTHING.y),
				MathHelper.lerp(tickDelta, animData.prevTickArrangements.EVERYTHING.z, animData.thisTickArrangements.EVERYTHING.z)
		);
		animData.prevFrameAnimationDeltas.EVERYTHING.setAngles(
				MarioAnimationData.lerpRadians(tickDelta, animData.prevTickArrangements.EVERYTHING.pitch, animData.thisTickArrangements.EVERYTHING.pitch),
				MarioAnimationData.lerpRadians(tickDelta, animData.prevTickArrangements.EVERYTHING.yaw, animData.thisTickArrangements.EVERYTHING.yaw),
				MarioAnimationData.lerpRadians(tickDelta, animData.prevTickArrangements.EVERYTHING.roll, animData.thisTickArrangements.EVERYTHING.roll)
		);

//		lerpBetweenArrangements(this.cloak, this.prevTickArrangements.CAPE, this.thisTickArrangements.CAPE, tickDelta, this.prevFrameAnimationDeltas.CAPE);
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
			MarioAnimationData.setupArrangement(part, arrangement);
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
