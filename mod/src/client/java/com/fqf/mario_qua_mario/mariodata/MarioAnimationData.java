package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.Arrangement;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.BodyPartAnimation;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.LimbAnimation;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario.mariodata.util.ArrangementSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.util.math.MathHelper.PI;
import static net.minecraft.util.math.MathHelper.TAU;

public class MarioAnimationData {
	public @NotNull ArrangementSet thisTickArrangements = new ArrangementSet(); // used for per-frame interpolation
	public @NotNull ArrangementSet prevFrameAnimationDeltas = new ArrangementSet(); // used to undo the previous frame's animation at the start of the next frame (for mod compatibility)



	private @Nullable Pose prevTickPose = new Pose();
	private @Nullable Pose thisTickPose = new Pose();

	private boolean changingAnim;
	public PlayermodelAnimation currentAnim;
	private PlayermodelAnimation prevAnim;
	private boolean trailingTick;
	private boolean isAnimating;
	private int animationTicks;
	public float headPitchOffset;
	public float headYawOffset;

	public void replaceAnimation(PlayermodelAnimation newAnim) {
		this.prevAnim = this.currentAnim;
		this.currentAnim = newAnim;
		this.changingAnim = true;
		this.animationTicks = 0;
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
			else if(this.prevAnim == null) {
				// Set up prevTickArrangements to accurately reflect Mario's current pose
				this.prevTickPose = new Pose(mario);
			}
			else {
				this.prevTickPose = this.thisTickPose; // Interpolation starts from 0
			}
		}
		else {
			if(this.currentAnim == null)
				return;
			else
				this.prevTickPose = this.thisTickPose; // Interpolation starts from 0
		}

		this.isAnimating = true;
		this.thisTickPose = null; // FIXME: this is DUMB!!!!!! >:(

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

		if(this.prevTickPose == null) this.prevTickPose = new Pose(mario);
		if(this.thisTickPose == null) this.thisTickPose = new Pose(mario);

		this.lerpPart(tickDelta, head, this.prevTickPose.HEAD, this.thisTickPose.HEAD);
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
				MathHelper.lerp(delta, prevTickArrangement.x, targetX),
				MathHelper.lerp(delta, prevTickArrangement.y, targetY),
				MathHelper.lerp(delta, prevTickArrangement.z, targetZ)
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
		else if(this.thisTickPose == null) thisTickArrangement = new Arrangement();
		else thisTickArrangement = this.thisTickPose.EVERYTHING;

		float pitch = MathHelper.lerp(tickDelta, prevTickArrangement.pitch, thisTickArrangement.pitch);
		float yaw = MathHelper.lerp(tickDelta, prevTickArrangement.yaw, thisTickArrangement.yaw);
		float roll = MathHelper.lerp(tickDelta, prevTickArrangement.roll, thisTickArrangement.roll);

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
		return MathHelper.lerp(delta, wrapRadians(start - offset), wrapRadians(end - offset)) + offset;
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
		public Pose(PlayerEntityModel<? extends LivingEntity> model) {
			setupArrangement(model.head, this.HEAD);
			setupArrangement(model.body, this.TORSO);
			setupArrangement(model.rightArm, this.RIGHT_ARM);
			setupArrangement(model.leftArm, this.LEFT_ARM);
			setupArrangement(model.rightLeg, this.RIGHT_LEG);
			setupArrangement(model.leftLeg, this.LEFT_LEG);
		}
		public Pose(AbstractClientPlayerEntity mario) {
			this(((PlayerEntityRenderer) MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(mario)).getModel());
		}
	}






	public static float lerpRadians(float delta, float start, float end) {
		return MathHelper.RADIANS_PER_DEGREE * MathHelper.lerpAngleDegrees(delta, MathHelper.DEGREES_PER_RADIAN * start, MathHelper.DEGREES_PER_RADIAN * end);
	}

	public static void undoFrame(ModelPart part, Arrangement arrangement, float multiplier) {
		part.pivotX += multiplier * arrangement.x;
		part.pivotY += multiplier * arrangement.y;
		part.pivotZ += multiplier * arrangement.z;
		part.pitch += multiplier * arrangement.pitch;
		part.yaw += multiplier * arrangement.yaw;
		part.roll += multiplier * arrangement.roll;
	}

	private static void animatePart(MarioPlayerData data, ModelPart part, Arrangement arrangement, Arrangement.Mutator mutator, float progress, boolean isMirrored) {
		if(mutator == null) return;
		setupArrangement(part, arrangement);
		applyMutator(data, arrangement, mutator, progress, isMirrored);
	}

	public static void animatePart(MarioPlayerData data, ModelPart part, Arrangement arrangement, BodyPartAnimation animation, float progress, boolean isMirrored) {
		if(animation == null) setupArrangement(part, arrangement);
		else animatePart(data, part, arrangement, animation.mutator(), progress, isMirrored);
	}

	public static void animatePart(MarioPlayerData data, ModelPart part, Arrangement arrangement, LimbAnimation animation, float progress, boolean isMirrored) {
		if(animation == null || animation.mutator() == null) setupArrangement(part, arrangement);
		else animatePart(data, part, arrangement, animation.mutator(), progress, isMirrored);
	}

	public static void applyMutator(MarioPlayerData data, Arrangement arrangement, Arrangement.Mutator mutator, float progress, boolean isMirrored) {
		float factor = MathHelper.DEGREES_PER_RADIAN;
		arrangement.setAngles(arrangement.pitch * factor, arrangement.yaw * factor, arrangement.roll * factor);

		float unmutatedX = arrangement.x;
		float unmutatedYaw = arrangement.yaw;
		mutator.mutate(data, arrangement, progress);
		if(isMirrored) {
			arrangement.x -= 2 * (arrangement.x - unmutatedX);
			arrangement.yaw -= 2 * (arrangement.yaw - unmutatedYaw);
		}

		factor = MathHelper.RADIANS_PER_DEGREE;
		arrangement.setAngles(arrangement.pitch * factor, arrangement.yaw * factor, arrangement.roll * factor);
	}

	public static boolean isArmBusy(BipedEntityModel.ArmPose armPose, BipedEntityModel.ArmPose otherArmPose) {
		return armPose.isTwoHanded() || otherArmPose.isTwoHanded() || switch (armPose) {
			case EMPTY, ITEM -> false;
			case BLOCK, BOW_AND_ARROW, THROW_SPEAR, CROSSBOW_CHARGE, CROSSBOW_HOLD, SPYGLASS, TOOT_HORN, BRUSH -> true;
		};
	}
}
