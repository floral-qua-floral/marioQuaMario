package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario.mariodata.util.ArrangementSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.util.math.MathHelper.*;

public class MarioAnimationData {
	public @NotNull ArrangementSet thisTickArrangements = new ArrangementSet(); // used for per-frame interpolation
	public @NotNull ArrangementSet prevFrameAnimationDeltas = new ArrangementSet(); // used to undo the previous frame's animation at the start of the next frame (for mod compatibility)



	private @Nullable Pose prevTickPose = new Pose();
	private @Nullable Pose thisTickPose = new Pose();

	private boolean changingAnim;
	public PlayermodelAnimation currentAnim;
	private PlayermodelAnimation prevAnim;
	private boolean reevaluateMirroring;
	private boolean isMirrored;
	private boolean trailingTick;
	private boolean isAnimating;
	private int animationTicks;
	public float headPitchOffset;
	public float headYawOffset;

	public void replaceAnimation(MarioPlayerData data, PlayermodelAnimation newAnim) {
		this.prevAnim = this.currentAnim;
		this.currentAnim = newAnim;
		if(this.shouldResetAnimation(data)) {
			this.changingAnim = true;
			this.animationTicks = 0;
		}
	}
	private boolean shouldResetAnimation(MarioPlayerData data) {
		boolean isSame = this.currentAnim == this.prevAnim;
		if(this.currentAnim == null || this.currentAnim.progressHandler() == null || this.currentAnim.progressHandler().resetter() == null || this.prevAnim == null)
			return !isSame;
		if(this.prevAnim.progressHandler() == null || this.prevAnim.progressHandler().resetter() == null)
			return this.currentAnim.progressHandler().resetter().shouldReset(data, null);
		return this.currentAnim.progressHandler().resetter().shouldReset(data, this.prevAnim.progressHandler().animationID());
	}
	public void tick(AbstractClientPlayerEntity mario) {
		this.isAnimating = false;
		this.trailingTick = false;

		if(this.changingAnim) {
			this.changingAnim = false;
			if(this.currentAnim == null && this.prevAnim == null) return;
			else if(this.currentAnim == null) {
				// For one more tick, Mario should interpolate from the last pose given by his previous animation, to
				// whatever his true pose actually is at the current frame.
				this.prevTickPose = this.thisTickPose;
				this.trailingTick = true;
				this.isAnimating = true;
				return;
			}
			else {
				this.reevaluateMirroring = true;
				if (this.prevAnim == null) {
					// Set up prevTickArrangements to accurately reflect Mario's current pose
					this.prevTickPose = new Pose(mario);
				} else {
					this.prevTickPose = this.thisTickPose; // Interpolation starts from 0
				}
			}
		}
		else {
			if(this.currentAnim == null)
				return;
			else
				this.prevTickPose = this.thisTickPose; // Interpolation starts from 0
		}

		this.isAnimating = true;
		this.thisTickPose = null;

		this.animationTicks++;
	}

	public boolean isAnimating(AbstractClientPlayerEntity mario) {
		return this.isAnimating && mario.mqm$getMarioData().isEnabled();
	}

	public void setAngles(
			float tickDelta, AbstractClientPlayerEntity mario,
			ModelPart head, ModelPart torso,
			ModelPart rightArm, ModelPart leftArm,
			ModelPart rightLeg, ModelPart leftLeg,
			BipedEntityModel.ArmPose rightArmPose, BipedEntityModel.ArmPose leftArmPose
	) {
		if(!this.isAnimating(mario)) return;

		if(this.reevaluateMirroring && this.currentAnim != null) {
			this.reevaluateMirroring = false;
			this.isMirrored = this.currentAnim.mirroringEvaluator() != null &&
					this.currentAnim.mirroringEvaluator().shouldMirror(mario.mqm$getMarioData(),
							isArmBusy(rightArmPose, leftArmPose), isArmBusy(leftArmPose, rightArmPose),
							head.yaw - torso.yaw);
		}

		if(this.prevTickPose == null) this.prevTickPose = new Pose(mario);
		if(this.thisTickPose == null) this.thisTickPose = this.makeAnimatedPose(mario, rightArmPose, leftArmPose);

		this.lerpPart(tickDelta, head, this.prevTickPose.HEAD, this.thisTickPose.HEAD);
		this.lerpPart(tickDelta, torso, this.prevTickPose.TORSO, this.thisTickPose.TORSO);
		this.lerpPart(tickDelta, rightArm, this.prevTickPose.RIGHT_ARM, this.thisTickPose.RIGHT_ARM);
		this.lerpPart(tickDelta, leftArm, this.prevTickPose.LEFT_ARM, this.thisTickPose.LEFT_ARM);
		this.lerpPart(tickDelta, rightLeg, this.prevTickPose.RIGHT_LEG, this.thisTickPose.RIGHT_LEG);
		this.lerpPart(tickDelta, leftLeg, this.prevTickPose.LEFT_LEG, this.thisTickPose.LEFT_LEG);
	}

	private Pose makeAnimatedPose(AbstractClientPlayerEntity mario, BipedEntityModel.ArmPose rightArmPose, BipedEntityModel.ArmPose leftArmPose) {
		Pose newPose = new Pose(mario);
		if(this.currentAnim != null) {
			MarioPlayerData data = mario.mqm$getMarioData();

			float progress;
			ProgressHandler handler = this.currentAnim.progressHandler();
			if(handler == null) progress = 1;
			else progress = handler.calculator().calculateProgress(data, this.animationTicks);

			this.mutate(newPose.EVERYTHING, this.currentAnim.entireBodyAnimation(), data, progress);
			this.mutate(newPose.HEAD, this.currentAnim.headAnimation(), data, progress);
			this.mutate(newPose.TORSO, this.currentAnim.torsoAnimation(), data, progress);
//
			this.conditionallyAnimateArm(
					newPose.RIGHT_ARM,
					this.isMirrored ? this.currentAnim.leftArmAnimation() : this.currentAnim.rightArmAnimation(),
					data, progress,
					rightArmPose, leftArmPose, newPose.TORSO
			);
			this.conditionallyAnimateArm(
					newPose.LEFT_ARM,
					this.isMirrored ? this.currentAnim.rightArmAnimation() : this.currentAnim.leftArmAnimation(),
					data, progress,
					leftArmPose, rightArmPose, newPose.TORSO
			);
			this.mutate(
					newPose.RIGHT_LEG,
					this.isMirrored ? this.currentAnim.leftLegAnimation() : this.currentAnim.rightLegAnimation(),
					data, progress
			);
			this.mutate(
					newPose.LEFT_LEG,
					this.isMirrored ? this.currentAnim.rightLegAnimation() : this.currentAnim.leftLegAnimation(),
					data, progress
			);
		}
		return newPose;
	}
	private void conditionallyAnimateArm(
			Arrangement arrangement, LimbAnimation limbAnimation, MarioPlayerData data, float progress,
			BipedEntityModel.ArmPose thisArmPose, BipedEntityModel.ArmPose otherArmPose, Arrangement torsoArrangement
	) {
		if(isArmBusy(thisArmPose, otherArmPose))
			arrangement.addPos(0, torsoArrangement.y, torsoArrangement.z);
		else
			this.mutate(arrangement, limbAnimation, data, progress);
	}
	private static boolean isArmBusy(BipedEntityModel.ArmPose thisArmPose, BipedEntityModel.ArmPose otherArmPose) {
		return thisArmPose.isTwoHanded() || otherArmPose.isTwoHanded() || switch (thisArmPose) {
			case EMPTY, ITEM -> false;
			default -> true;
		};
	}
	private void mutate(Arrangement arrangement, PlayermodelAnimation.MutatorContainer container, MarioPlayerData data, float progress) {
		if(container == null) return;
		Arrangement.Mutator mutator = container.mutator();
		if(mutator != null) this.mutate(arrangement, mutator, data, progress);
	}
	private void mutate(Arrangement arrangement, Arrangement.Mutator mutator, MarioPlayerData data, float progress) {
		// Convert arrangement to degrees
		convertAngles(arrangement, true);

		float unmutatedX = arrangement.x;
		float unmutatedY = arrangement.y;
		float unmutatedZ = arrangement.z;
		float unmutatedYaw = arrangement.yaw;
		float unmutatedRoll = arrangement.roll;
		mutator.mutate(data, arrangement, progress);
		if(isMirrored) {
			arrangement.x -= 2 * (arrangement.x - unmutatedX);
			arrangement.yaw -= 2 * (arrangement.yaw - unmutatedYaw);
			arrangement.roll -= 2 * (arrangement.roll - unmutatedRoll);
		}
		float horizontalScale = data.getPowerUp().ANIMATION_WIDTH_FACTOR * data.getCharacter().ANIMATION_WIDTH_FACTOR;
		float verticalScale = data.getPowerUp().ANIMATION_HEIGHT_FACTOR * data.getCharacter().ANIMATION_HEIGHT_FACTOR;
		arrangement.x = unmutatedX + (arrangement.x - unmutatedX) * horizontalScale;
		arrangement.y = unmutatedY + (arrangement.y - unmutatedY) * verticalScale;
		arrangement.z = unmutatedZ + (arrangement.z - unmutatedZ) * horizontalScale;

		// Convert arrangement back to radians
		convertAngles(arrangement, false);
	}
	private static void convertAngles(Arrangement arrangement, boolean isToDegrees) {
		float factor = isToDegrees ? DEGREES_PER_RADIAN : RADIANS_PER_DEGREE;
		arrangement.setAngles(
				arrangement.pitch * factor,
				arrangement.yaw * factor,
				arrangement.roll * factor
		);
	}

	private void lerpPart(float delta, ModelPart part, Arrangement prevTickArrangement, Arrangement thisTickArrangement) {
		float targetX, targetY, targetZ, targetPitch, targetYaw, targetRoll;
		if(this.trailingTick) {
			targetX = part.pivotX; targetY = part.pivotY; targetZ = part.pivotZ;
			targetPitch = part.pitch; targetYaw = part.yaw; targetRoll = part.roll;
		}
		else {
			targetX = thisTickArrangement.x; targetY = thisTickArrangement.y; targetZ = thisTickArrangement.z;
			targetPitch = thisTickArrangement.pitch; targetYaw = thisTickArrangement.yaw; targetRoll = thisTickArrangement.roll;
		}

		part.setPivot(
				lerp(delta, prevTickArrangement.x, targetX),
				lerp(delta, prevTickArrangement.y, targetY),
				lerp(delta, prevTickArrangement.z, targetZ)
		);
		part.setAngles(
				slerpRadians(delta, prevTickArrangement.pitch, targetPitch),
				slerpRadians(delta, prevTickArrangement.yaw, targetYaw),
				slerpRadians(delta, prevTickArrangement.roll, targetRoll)
		);
	}

	public void rotateTotalPlayermodel(
			float tickDelta, AbstractClientPlayerEntity mario, MatrixStack matrixStack
	) {
		if(!this.isAnimating(mario)) return;

		Arrangement prevTickArrangement, thisTickArrangement;
		if(this.prevTickPose == null) prevTickArrangement = new Arrangement();
		else prevTickArrangement = this.prevTickPose.EVERYTHING;

		if(this.trailingTick) thisTickArrangement = new Arrangement();
		else if(this.thisTickPose == null) {
			thisTickArrangement = new Arrangement();
			if(this.currentAnim != null)
				this.mutate(thisTickArrangement, this.currentAnim.entireBodyAnimation(), mario.mqm$getMarioData(), 0);
		}
		else thisTickArrangement = this.thisTickPose.EVERYTHING;

		float pitch = lerp(tickDelta, prevTickArrangement.pitch, thisTickArrangement.pitch);
		float yaw = lerp(tickDelta, prevTickArrangement.yaw, thisTickArrangement.yaw);
		float roll = lerp(tickDelta, prevTickArrangement.roll, thisTickArrangement.roll);

		this.headPitchOffset = pitch;
		this.headYawOffset = yaw;

		matrixStack.multiply(RotationAxis.POSITIVE_X.rotation(pitch));
		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(yaw));
		matrixStack.multiply(RotationAxis.POSITIVE_Z.rotation(roll));
	}

	private static void setupArrangement(ModelPart from, Arrangement to) {
		to.setPos(from.pivotX, from.pivotY, from.pivotZ);
		to.setAngles(from.pitch, from.yaw, from.roll);
	}

	private static float wrapRadians(float radians) {
		float wrapped = radians % TAU;
		if(wrapped >= PI) wrapped -= TAU;
		if(wrapped < -PI) wrapped += TAU;
		return wrapped;
	}

	private static float slerpRadians(float delta, float start, float end) {
		return start + wrapRadians(end - start) * delta;
	}

	private static float discontinuousSlerp(float delta, float start, float end, float hole) {
		float offset = hole - PI;
		return lerp(delta, wrapRadians(start - offset), wrapRadians(end - offset)) + offset;
	}

	private static class Pose {
		public final Arrangement EVERYTHING = new Arrangement();
		public final Arrangement HEAD = new Arrangement();
		public final Arrangement TORSO = new Arrangement();

		public final Arrangement RIGHT_ARM = new Arrangement();
		public final Arrangement LEFT_ARM = new Arrangement();

		public final Arrangement RIGHT_LEG = new Arrangement();
		public final Arrangement LEFT_LEG = new Arrangement();

		public Pose() {

		}

		public Pose(AbstractClientPlayerEntity mario) {
			PlayerEntityModel<? extends LivingEntity> model =
					((PlayerEntityRenderer) MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(mario)).getModel();
			setupArrangement(model.head, this.HEAD);
			setupArrangement(model.body, this.TORSO);
			setupArrangement(model.rightArm, this.RIGHT_ARM);
			setupArrangement(model.leftArm, this.LEFT_ARM);
			setupArrangement(model.rightLeg, this.RIGHT_LEG);
			setupArrangement(model.leftLeg, this.LEFT_LEG);
		}
	}
}
