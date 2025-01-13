package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.Arrangement;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.BodyPartAnimation;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.LimbAnimation;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario.mariodata.MarioAnimationData;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.mariodata.util.ArrangementSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;
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
		MarioAnimationData animationData = mario.mqm$getAnimationData();
		if(!mario.mqm$getMarioData().isEnabled() || !animationData.animatedLastFrame) return;

		animationData.animatedLastFrame = false;
		undoFrame(this.head, animationData.prevFrameAnimationDeltas.HEAD);
		undoFrame(this.body, animationData.prevFrameAnimationDeltas.BODY);

		undoFrame(this.rightArm, animationData.prevFrameAnimationDeltas.RIGHT_ARM);
		undoFrame(this.leftArm, animationData.prevFrameAnimationDeltas.LEFT_ARM);

		undoFrame(this.rightLeg, animationData.prevFrameAnimationDeltas.RIGHT_LEG);
		undoFrame(this.leftLeg, animationData.prevFrameAnimationDeltas.LEFT_LEG);
	}

	@Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/BipedEntityModel;setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", shift = At.Shift.AFTER))
	private void setAnglesHook(T livingEntity, float f, float g, float h, float i, float j, CallbackInfo ci) {
		AbstractClientPlayerEntity mario = (AbstractClientPlayerEntity) livingEntity;
		MarioPlayerData data = mario.mqm$getMarioData();
		MarioAnimationData animData = mario.mqm$getAnimationData();
		PlayermodelAnimation animation = data.getAction().ANIMATION;

		if(!data.isEnabled() || animation == null) return;

		if(data.tickAnimation) {
			data.tickAnimation = false;

			animData.prevTickArrangements = animData.thisTickArrangements;
			animData.thisTickArrangements = new ArrangementSet();

			if(data.resetAnimation) {
				data.resetAnimation = false;
				animData.prevTickArrangements.EVERYTHING.setPos(0, 0, 0);
				animData.prevTickArrangements.EVERYTHING.setAngles(0, 0, 0);
				setupArrangement(this.head, animData.prevTickArrangements.HEAD);
				setupArrangement(this.body, animData.prevTickArrangements.BODY);
				setupArrangement(this.rightArm, animData.prevTickArrangements.RIGHT_ARM);
				setupArrangement(this.leftArm, animData.prevTickArrangements.LEFT_ARM);
				setupArrangement(this.rightLeg, animData.prevTickArrangements.RIGHT_LEG);
				setupArrangement(this.leftLeg, animData.prevTickArrangements.LEFT_LEG);
				setupArrangement(this.cloak, animData.prevTickArrangements.CAPE);
				animData.animationTicks = 0;
			}

			float progress = animation.progressCalculator().calculateProgress(data, animData.animationTicks);
			animData.animationTicks++;

			if(animation.wholeMutator() != null) {
				animData.thisTickArrangements.EVERYTHING.setPos(0, 0, 0);
				animData.thisTickArrangements.EVERYTHING.setAngles(0, 0, 0);
				applyMutator(data, animData.thisTickArrangements.EVERYTHING, animation.wholeMutator(), progress);
			}

			animatePart(data, this.head, animData.thisTickArrangements.HEAD, animation.headAnimation(), progress);
			animatePart(data, this.body, animData.thisTickArrangements.BODY, animation.torsoAnimation(), progress);

			intelligentlyAnimateArm(data, animData, this.rightArm, animData.thisTickArrangements.RIGHT_ARM, animation.rightArmAnimation(), progress, true);
			intelligentlyAnimateArm(data, animData, this.leftArm, animData.thisTickArrangements.LEFT_ARM, animation.leftArmAnimation(), progress, false);

			animatePart(data, this.rightLeg, animData.thisTickArrangements.RIGHT_LEG, animation.rightLegAnimation(), progress);
			animatePart(data, this.leftLeg, animData.thisTickArrangements.LEFT_LEG, animation.leftLegAnimation(), progress);
		}

		animData.animatedLastFrame = true;
		float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
//		MarioQuaMario.LOGGER.info("TickDelta={}", MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true));

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
	private static void setupArrangement(ModelPart from, Arrangement to) {
		to.x = from.pivotX; to.y = from.pivotY; to.z = from.pivotZ;
		to.pitch = from.pitch; to.yaw = from.yaw; to.roll = from.roll;
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
	private static void undoFrame(ModelPart part, Arrangement arrangement) {
		part.pivotX -= arrangement.x;
		part.pivotY -= arrangement.y;
		part.pivotZ -= arrangement.z;
		part.pitch -= arrangement.pitch;
		part.yaw -= arrangement.yaw;
		part.roll -= arrangement.roll;
	}

	@Unique
	private static void animatePart(MarioPlayerData data, ModelPart part, Arrangement arrangement, Arrangement.Mutator mutator, float progress) {
		if(mutator == null) return;
		setupArrangement(part, arrangement);
		applyMutator(data, arrangement, mutator, progress);
	}
	@Unique
	private static void animatePart(MarioPlayerData data, ModelPart part, Arrangement arrangement, BodyPartAnimation animation, float progress) {
		if(animation == null || animation.mutator() == null) setupArrangement(part, arrangement);
		else animatePart(data, part, arrangement, animation.mutator(), progress);
	}
	@Unique
	private static void animatePart(MarioPlayerData data, ModelPart part, Arrangement arrangement, LimbAnimation animation, float progress) {
		if(animation == null || animation.mutator() == null) setupArrangement(part, arrangement);
		else animatePart(data, part, arrangement, animation.mutator(), progress);
	}

	@Unique
	private void intelligentlyAnimateArm(MarioPlayerData data, MarioAnimationData animData, ModelPart part, Arrangement arrangement, LimbAnimation animation, float progress, boolean isRight) {
		ArmPose thisArmPose = isRight ? this.rightArmPose : this.leftArmPose;
		ArmPose otherArmPose = isRight ? this.leftArmPose : this.rightArmPose;
		if(!shouldAnimateArm(thisArmPose, otherArmPose)) {
			setupArrangement(part, arrangement);
			arrangement.addPos(animData.thisTickArrangements.BODY.x, animData.thisTickArrangements.BODY.y, animData.thisTickArrangements.BODY.z);
		}
		else animatePart(data, part, arrangement, animation, progress);
	}

	@Unique
	private static void applyMutator(MarioPlayerData data, Arrangement arrangement, Arrangement.Mutator mutator, float progress) {
		float factor = MathHelper.DEGREES_PER_RADIAN;
		arrangement.setAngles(arrangement.pitch * factor, arrangement.yaw * factor, arrangement.roll * factor);

		mutator.mutate(data, arrangement, progress);

		factor = MathHelper.RADIANS_PER_DEGREE;
		arrangement.setAngles(arrangement.pitch * factor, arrangement.yaw * factor, arrangement.roll * factor);
	}

	@Unique
	private static boolean shouldAnimateArm(ArmPose armPose, ArmPose otherArmPose) {
		return !armPose.isTwoHanded() && !otherArmPose.isTwoHanded() && switch (armPose) {
			case EMPTY, ITEM -> true;
			case BLOCK, BOW_AND_ARROW, THROW_SPEAR, CROSSBOW_CHARGE, CROSSBOW_HOLD, SPYGLASS, TOOT_HORN, BRUSH -> false;
		};
	}
}
