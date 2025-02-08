package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario.util.Easing;
import com.tom.cpm.client.CustomPlayerModelsClient;
import com.tom.cpm.shared.config.Player;
import com.tom.cpm.shared.definition.ModelDefinition;
import com.tom.cpm.shared.model.RootModelType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static net.minecraft.util.math.MathHelper.*;

public class MarioAnimationData {
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
	public final Arrangement TAIL_ARRANGEMENT = new Arrangement();
	private final Arrangement TAIL_TEMP_ARRANGEMENT = new Arrangement();

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
			ModelPart tail,
			BipedEntityModel.ArmPose rightArmPose, BipedEntityModel.ArmPose leftArmPose
	) {
		if(this.isAnimating(mario)) {
			if (this.reevaluateMirroring && this.currentAnim != null) {
				this.reevaluateMirroring = false;
				this.isMirrored = this.currentAnim.mirroringEvaluator() != null &&
						this.currentAnim.mirroringEvaluator().shouldMirror(mario.mqm$getMarioData(),
								isArmBusy(rightArmPose, leftArmPose), isArmBusy(leftArmPose, rightArmPose),
								head.yaw - torso.yaw);
			}

			if (this.prevTickPose == null) this.prevTickPose = new Pose(mario);
			if (this.thisTickPose == null) this.thisTickPose = this.makeAnimatedPose(mario, rightArmPose, leftArmPose);

			this.lerpPart(tickDelta, head, this.prevTickPose.HEAD, this.thisTickPose.HEAD);
			this.lerpPart(tickDelta, torso, this.prevTickPose.TORSO, this.thisTickPose.TORSO);
			this.lerpPart(tickDelta, rightArm, this.prevTickPose.RIGHT_ARM, this.thisTickPose.RIGHT_ARM);
			this.lerpPart(tickDelta, leftArm, this.prevTickPose.LEFT_ARM, this.thisTickPose.LEFT_ARM);
			this.lerpPart(tickDelta, rightLeg, this.prevTickPose.RIGHT_LEG, this.thisTickPose.RIGHT_LEG);
			this.lerpPart(tickDelta, leftLeg, this.prevTickPose.LEFT_LEG, this.thisTickPose.LEFT_LEG);

//			setupTailArrangement(this.TAIL_ARRANGEMENT, torso.pivotX, torso.pivotY, torso.pivotZ, torso.pitch, torso.yaw, torso.roll, rightLeg.pitch, leftLeg.pitch);
//			tail.setPivot(this.TAIL_ARRANGEMENT.x, this.TAIL_ARRANGEMENT.y, this.TAIL_ARRANGEMENT.z);
//			tail.setAngles(this.TAIL_ARRANGEMENT.pitch, this.TAIL_ARRANGEMENT.yaw, this.TAIL_ARRANGEMENT.roll);
//			this.lerpPart(tickDelta, tail, this.prevTickPose.TAIL, this.thisTickPose.TAIL);
		}
		else if(mario.mqm$getMarioData().isEnabled()) {
			this.setupTailArrangement(this.TAIL_ARRANGEMENT, mario.mqm$getMarioData(), torso.pivotX, torso.pivotY, torso.pivotZ, torso.pitch, torso.yaw, torso.roll, rightLeg.pitch, leftLeg.pitch);
			tail.setPivot(this.TAIL_ARRANGEMENT.x, this.TAIL_ARRANGEMENT.y, this.TAIL_ARRANGEMENT.z);
			tail.setAngles(this.TAIL_ARRANGEMENT.pitch, this.TAIL_ARRANGEMENT.yaw, this.TAIL_ARRANGEMENT.roll);
		}
	}

	private Pose makeAnimatedPose(AbstractClientPlayerEntity mario, BipedEntityModel.ArmPose rightArmPose, BipedEntityModel.ArmPose leftArmPose) {
		Pose newPose = new Pose(mario);
		if(this.currentAnim != null) {
			MarioPlayerData data = mario.mqm$getMarioData();

			float progress = this.calculateProgress(data);

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

			this.setupTailArrangement(newPose.TAIL, data, newPose.TORSO.x, newPose.TORSO.y, newPose.TORSO.z, newPose.TORSO.pitch, newPose.TORSO.yaw, newPose.TORSO.roll, newPose.RIGHT_LEG.pitch, newPose.LEFT_LEG.pitch);
			this.mutate(newPose.TAIL, this.currentAnim.capeAnimation(), data, progress);
		}
		return newPose;
	}
	private float calculateProgress(MarioPlayerData data) {
		ProgressHandler handler = this.currentAnim.progressHandler();
		if(handler == null) return 1;
		else return handler.calculator().calculateProgress(data, this.animationTicks);
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
	private void setupTailArrangement(
			Arrangement arrangement, MarioPlayerData data,
			float torsoX, float torsoY, float torsoZ,
			float torsoPitch, float torsoYaw, float torsoRoll,
			float rightLegPitch, float leftLegPitch
	) {
		float capeY = 0;

		Player<?> pl = CustomPlayerModelsClient.INSTANCE.manager.getBoundPlayer();
		if(pl != null) {
			ModelDefinition def = pl.getModelDefinition();
			if(def != null && def.hasRoot(RootModelType.CAPE)) {
				capeY = def.getModelElementFor(RootModelType.CAPE).get().posN.y;
			}
		}

		MarioQuaMario.LOGGER.info("Cape Y: {}", capeY);

		Vector3f tailPosRelativeToTorso = new Vector3f(0, capeY, 2).rotateX(torsoPitch).rotateY(torsoYaw).rotateZ(torsoRoll);
		arrangement.setPos(
				torsoX + tailPosRelativeToTorso.x,
				torsoY + tailPosRelativeToTorso.y - capeY,
				torsoZ + tailPosRelativeToTorso.z
		);

		arrangement.setAngles(-torsoPitch, torsoYaw, torsoRoll);
		if(this.currentAnim == null || this.currentAnim.capeAnimation() == null || this.currentAnim.capeAnimation().shouldSwingWithMovement()) {
			float swing = (leftLegPitch - rightLegPitch) * 0.26F;
			float lift = data.getMario().isOnGround()
					? 1 - Easing.SINE_IN_OUT.ease(Easing.clampedRangeToProgress(data.getForwardVel(), 0, 0.55))
					: Easing.EXPO_IN_OUT.ease(Easing.clampedRangeToProgress(data.getYVel(), 0.87, -0.85), 0.55F, -0.8F);
			arrangement.addAngles(
					0.65F * lift * HALF_PI,
					swing * 0.78F,
					swing * 1.2F * Math.abs(lift)
			);
		}
	}
	public void animateTail(
			float tickDelta, AbstractClientPlayerEntity mario,
			ModelPart tail, ModelPart torso, ModelPart rightLeg, ModelPart leftLeg
	) {
		boolean animating = this.isAnimating(mario);

		if(!animating || this.trailingTick) {
			this.setupTailArrangement(this.TAIL_ARRANGEMENT, mario.mqm$getMarioData(), torso.pivotX, torso.pivotY, torso.pivotZ, torso.pitch, torso.yaw, torso.roll, rightLeg.pitch, leftLeg.pitch);
			tail.setPivot(this.TAIL_ARRANGEMENT.x, this.TAIL_ARRANGEMENT.y, this.TAIL_ARRANGEMENT.z);
			tail.setAngles(this.TAIL_ARRANGEMENT.pitch, this.TAIL_ARRANGEMENT.yaw, this.TAIL_ARRANGEMENT.roll);
		}

		if(animating) {
			assert this.prevTickPose != null && this.thisTickPose != null;
			this.lerpPart(tickDelta, tail, this.prevTickPose.TAIL, this.thisTickPose.TAIL);
		}
		tail.yaw += PI;
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
			if(this.currentAnim != null) {
				MarioPlayerData data = mario.mqm$getMarioData();
				this.mutate(thisTickArrangement, this.currentAnim.entireBodyAnimation(), data, this.calculateProgress(data));
			}
		}
		else thisTickArrangement = this.thisTickPose.EVERYTHING;

		float pitch = slerpRadians(tickDelta, prevTickArrangement.pitch, thisTickArrangement.pitch);
		float yaw = slerpRadians(tickDelta, prevTickArrangement.yaw, thisTickArrangement.yaw);
		float roll = slerpRadians(tickDelta, prevTickArrangement.roll, thisTickArrangement.roll);

		this.headPitchOffset = pitch;
		this.headYawOffset = yaw;

		matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(yaw));
		if(pitch != 0 || roll != 0) {
			double pivotHeightFactor;
			if(this.currentAnim == null || this.currentAnim.entireBodyAnimation() == null) {
				if(this.prevAnim == null || this.prevAnim.entireBodyAnimation() == null) pivotHeightFactor = 0.5;
				else pivotHeightFactor = this.prevAnim.entireBodyAnimation().pivotHeightFactor();
			}
			else pivotHeightFactor = this.currentAnim.entireBodyAnimation().pivotHeightFactor();

			double halfHeight = mario.getBoundingBox(EntityPose.STANDING).getLengthY() * pivotHeightFactor;
			matrixStack.translate(0, halfHeight, 0);
			matrixStack.multiply(RotationAxis.POSITIVE_X.rotation(pitch));
			matrixStack.multiply(RotationAxis.POSITIVE_Z.rotation(roll));
			matrixStack.translate(0, -halfHeight, 0);
		}
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

		public final Arrangement TAIL = new Arrangement();

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
			mario.mqm$getAnimationData().setupTailArrangement(this.TAIL, mario.mqm$getMarioData(), model.body.pivotX, model.body.pivotY, model.body.pivotZ, model.body.pitch, model.body.yaw, model.body.roll, model.rightLeg.pitch, model.leftLeg.pitch);
		}
	}

	public void launderCapeTransformations(MatrixStack matrices) {
//		matrices.translate(0.0, 0.0, 0.0);
//		matrices.translate(0F, 0F, 0F);
//		matrices.translate(this.TAIL_ARRANGEMENT.x / 16F, this.TAIL_ARRANGEMENT.y / 16F, this.TAIL_ARRANGEMENT.z / 16F);
//		matrices.multiply(RotationAxis.POSITIVE_X.rotation(this.TAIL_ARRANGEMENT.pitch));
//		matrices.multiply(RotationAxis.POSITIVE_Y.rotation(this.TAIL_ARRANGEMENT.yaw));
//		matrices.multiply(RotationAxis.POSITIVE_Z.rotation(this.TAIL_ARRANGEMENT.roll));
	}
}
