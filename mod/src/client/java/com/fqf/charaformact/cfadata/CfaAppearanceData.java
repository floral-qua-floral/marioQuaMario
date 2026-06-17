package com.fqf.charaformact.cfadata;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.appearance.AppearanceRenderer;
import com.fqf.charaformact.appearance.ClientAppearanceCollector;
import com.fqf.charaformact.appearance.ParsedClientAppearance;
import com.fqf.charaformact.cfadata.util.ActiveAnimation;
import com.fqf.charaformact.cfadata.util.AdvancedArrangement;
import com.fqf.charaformact.cfadata.util.AdvancedPosture;
import com.fqf.charaformact.registries.actions.ParsedAnimation;
import com.fqf.charaformact.registries.power_granting.CharacterFormCombo;
import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact_api.appearance.TransformationInstructions;
import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.Arrangement;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Pair;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static net.minecraft.util.math.MathHelper.DEGREES_PER_RADIAN;
import static net.minecraft.util.math.MathHelper.HALF_PI;

public class CfaAppearanceData<CfaDataType extends CfaPlayerData & CfaAnimatingData & CfaClientDataImpl> {
	public final AbstractClientPlayerEntity PLAYER;
	public final CfaDataType DATA;

	private @Nullable ParsedClientAppearance appearance, flickerModel;
	private @Nullable PlayerEntityRenderer renderer, flickerRenderer;

	public @Nullable ActiveAnimation actionAnimation;
	public @Nullable ActiveAnimation forcedAnimation;
	private long forcedAnimationEndTime;

	private long forceInterpolationTime;

	private long prevPosturingTick;
	private AdvancedPosture prevFramePosture;

	private long prevArrangingTick;
	private AdvancedArrangement prevFrameModelArrangement;
	private AdvancedArrangement thisFrameModelArrangement;
	private float originalHeadPitch, originalHeadYaw;

	public boolean doingFirstPersonHand;

	private long flickerUntil;
	private boolean flickering;

	public CfaAppearanceData(CfaDataType data) {
		this.PLAYER = data.getPlayer();
		this.DATA = data;

		this.flickerUntil = Long.MIN_VALUE;
		this.prevFramePosture = new AdvancedPosture();
		this.prevFrameModelArrangement = new AdvancedArrangement();
		this.thisFrameModelArrangement = new AdvancedArrangement();
	}

	public void tick() {
		long time = this.PLAYER.getWorld().getTime();

		if(this.forcedAnimation != null && time >= this.forcedAnimationEndTime) {
			this.forcedAnimation = null;
			this.forceInterpolation();
			if(this.actionAnimation instanceof ActiveAnimation.Interpolated interpolatedAnimation) {
				// Force the action animation to interpolate from the last frame of the override animation, rather than
				// from its own last tick posture. We put these in the "to" variable because on the first frame of this
				// tick it will be shifting these into the "from" variables.
				interpolatedAnimation.fromPosture = this.prevFramePosture;
				interpolatedAnimation.toPosture = this.prevFramePosture;
				interpolatedAnimation.fromModelArrangement = this.prevFrameModelArrangement;
				interpolatedAnimation.toModelArrangement = this.prevFrameModelArrangement;
			}
		}

		if(this.flickerUntil > time) {
			long difference = this.flickerUntil - time;
			this.flickering = MathHelper.floor(difference / 3F) % 2 == 0;
		}
		else this.flickering = false;
	}

	public boolean hasAppearance() {
		return this.getAppearance() != null;
	}

	public ParsedClientAppearance getAppearance() {
		return this.getAppearance(true);
	}

	public ParsedClientAppearance getAppearance(boolean allowFlicker) {
		if(allowFlicker && this.flickering) return this.flickerModel;
		return this.appearance;
	}

	public PlayerEntityRenderer getRenderer() {
		if(this.flickering) return this.flickerRenderer;
		return this.renderer;
	}

	public void conditionallyFlicker() {
		this.flickerUntil = this.PLAYER.getWorld().getTime() + 9L;
	}

	public void updateAppearance() {
		this.flickerModel = this.appearance;
		this.flickerRenderer = this.renderer;

		@Nullable Pair<ParsedClientAppearance, AppearanceRenderer> newModelAndRenderer = null;

		if(this.DATA.isEnabled()) {
			ParsedCharacter character = this.DATA.getCharacter();
			ParsedForm form = this.DATA.getForm();
			newModelAndRenderer = ClientAppearanceCollector.INSTANCE.get(
					new CharacterFormCombo(character, form));
			if(newModelAndRenderer == null) {
				CharaFormAct.LOGGER.warn("Player {} could not find a playermodel for {} in form {}!",
						this.PLAYER, character, form);
				newModelAndRenderer = ClientAppearanceCollector.INSTANCE.get(
						new CharacterFormCombo(character, character.INITIAL_FORM));
			}
		}

		if(newModelAndRenderer == null) newModelAndRenderer = new Pair<>(null, null);
		this.appearance = newModelAndRenderer.getLeft();
		this.renderer = newModelAndRenderer.getRight();

		this.conditionallyFlicker();
	}

	public void updateAction() {
		ActiveAnimation newActionAnim = ActiveAnimation.of(this, this.DATA.getAction().ANIMATION,
				this.prevFramePosture, this.prevFrameModelArrangement);
		boolean isOrWasAnimating = newActionAnim != null || actionAnimation != null;
		this.actionAnimation = newActionAnim;
		if(isOrWasAnimating) this.forceInterpolation();
	}

	// This is an currentAnimation triggered during client execution of an attack interception, as opposed to an currentAnimation
	// that is registered to an Action. As a result, we can't really parse it in advance. Fortunately currentAnimation parsing
	// is very light and easy and does not involve any actual registries, so we can just do it on the fly.
	public void triggerAnimation(@NotNull AnimationDefinition definition, int duration) {
		this.forcedAnimation = ActiveAnimation.of(this, new ParsedAnimation(definition),
				this.prevFramePosture, this.prevFrameModelArrangement);
		this.forcedAnimationEndTime = this.PLAYER.getWorld().getTime() + duration;

		// Attack interceptions, particularly those associated with an action instead of a form, may wish to continue from
		// their action's current currentAnimation. We should respect this, this is perfectly legitimate!
		if(!this.forcedAnimation.EXECUTION_FLAGS.contains(AnimationFlag.Execution.DO_NOT_RESET_PROGRESS))
			this.forceInterpolation();
	}

	public @Nullable ActiveAnimation getCurrentAnimation() {
		if(this.forcedAnimation != null) return this.forcedAnimation;
		else return this.actionAnimation;
	}

	private void forceInterpolation() {
		this.forceInterpolationTime = this.PLAYER.getWorld().getTime() + 1;
	}

	public void arrangeModel(MatrixStack matrices, float tickDelta) {
		// This occurs first in the frame, called by LivingEntityRenderer.render.
		// Then it calls setAngles, which first executes BipedEntityModel.setAngles, triggering head counter-rotation.
		// Last it does PlayerEntity.setAngles (post-super), which triggers posture animate.

		// As such, playermodel arrangement occurs before we know anything about the player's Posture. This is mostly
		// unavoidable. We could probably adjust the MatrixStack later in rendering, after we've positioned the limbs,
		// but at that point we've missed the correct timing for head counter-rotation, which we want to be applying
		// BEFORE vanilla does its aiming animations. Head counter-rotation must occur before vanilla decides limb
		// angles, but it can only be done after playermodel arrangement. And posture mutation must occur after vanilla
		// decides limb angles, so this is the only possible order.

		this.thisFrameModelArrangement.setPos(0, 0, 0);
		this.thisFrameModelArrangement.setAngles(0, 0, 0);

		long worldTime = this.PLAYER.getWorld().getTime();
		boolean isFirstOfTick = worldTime > this.prevArrangingTick;
		if(isFirstOfTick) {
			this.prevArrangingTick = worldTime;
		}

		this.thisFrameModelArrangement = new AdvancedArrangement();
		boolean forceWrappedInterpolation = worldTime <= this.forceInterpolationTime;
		boolean hasInterpolated;

		ActiveAnimation currentAnimation = this.getCurrentAnimation();
		if(currentAnimation != null) {
			hasInterpolated = !currentAnimation.ANIMATION.FLAGS.contains(AnimationFlag.NOT_INTERPOLATED);
			currentAnimation.mutateModelArrangement(this.thisFrameModelArrangement, worldTime, tickDelta, isFirstOfTick, forceWrappedInterpolation);
		}
		else hasInterpolated = false;

		if(!hasInterpolated && forceWrappedInterpolation) {
			// Maybe we don't actually need to make a new Arrangement as the lerp target? Feeding it itself should work?
			AdvancedArrangement lerpTo = new AdvancedArrangement();
			lerpTo.setPos(this.thisFrameModelArrangement.x, this.thisFrameModelArrangement.y, this.thisFrameModelArrangement.z);
			lerpTo.setAngles(this.thisFrameModelArrangement.pitch, this.thisFrameModelArrangement.yaw, this.thisFrameModelArrangement.roll);
			this.thisFrameModelArrangement.wrappedLerpRadians(tickDelta, this.prevFrameModelArrangement, lerpTo);
		}

		this.thisFrameModelArrangement.x *= this.DATA.getHorizontalAnimationScale();
		this.thisFrameModelArrangement.y *= this.DATA.getVerticalAnimationScale();
		this.thisFrameModelArrangement.z *= this.DATA.getHorizontalAnimationScale();

		this.prevFrameModelArrangement = new AdvancedArrangement();
		this.prevFrameModelArrangement.setPos(
				this.thisFrameModelArrangement.x, this.thisFrameModelArrangement.y, this.thisFrameModelArrangement.z);
		this.prevFrameModelArrangement.setAngles(
				this.thisFrameModelArrangement.pitch, this.thisFrameModelArrangement.yaw, this.thisFrameModelArrangement.roll);

		matrices.translate(this.thisFrameModelArrangement.x / 16F, this.thisFrameModelArrangement.y / 16F, this.thisFrameModelArrangement.z / 16F);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotation(this.thisFrameModelArrangement.yaw));
		double halfHeight = this.PLAYER.getHeight() * 0.5F;
		matrices.translate(0, halfHeight, 0);
		matrices.multiply(RotationAxis.POSITIVE_X.rotation(this.thisFrameModelArrangement.pitch));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotation(this.thisFrameModelArrangement.roll));
		matrices.translate(0, -halfHeight, 0);
	}

	private static final float ALMOST_HALF_PI = 0.99F * HALF_PI;
	private static final float MAX_HEAD_YAW_RADIANS = HALF_PI * 0.66F;

	public static float counterRotateYaw(float headYaw, Arrangement againstArrangement) {
		return MathHelper.clamp(AdvancedArrangement.wrapRadians(headYaw + againstArrangement.yaw), -MAX_HEAD_YAW_RADIANS, MAX_HEAD_YAW_RADIANS);
	}
	public static float counterRotatePitch(float headPitch, Arrangement againstArrangement) {
		return MathHelper.clamp(AdvancedArrangement.wrapRadians(headPitch + againstArrangement.pitch), -ALMOST_HALF_PI, ALMOST_HALF_PI);
	}

	public float counterRotateHead(ModelPart head, float assigningPitch) {
		// Note: The counter-rotation applied here gets undone before rendering IF we're in an Interpolated action.
		// However, it's still necessary to do it here, because the rotation applied here does still affect some
		// vanilla animations! Particularly aiming animations - bow, crossbow, spyglass. So we need to do this logic
		// always!
		this.originalHeadPitch = assigningPitch;
		this.originalHeadYaw = head.yaw;

		ActiveAnimation currentAnimation = this.getCurrentAnimation();
		if(currentAnimation != null && currentAnimation.ANIMATION.FLAGS.contains(AnimationFlag.NO_HEAD_COUNTERROTATION))
			return assigningPitch;

		head.yaw = counterRotateYaw(head.yaw, this.thisFrameModelArrangement);
		return counterRotatePitch(assigningPitch, this.thisFrameModelArrangement);
	}

	public void animate(PlayerEntityModel<?> model, float tickDelta) {
		try {
			if(this.doingFirstPersonHand) {
				this.animateFirstPerson(model, tickDelta);
				return;
			}


//			model.head.pitch = this.counterRotatePitch(model.head.pitch);
//			model.head.yaw = this.counterRotateYaw(model.head.yaw);

			boolean rightArmBusy = isArmBusy(model.rightArmPose, model.leftArmPose);
			boolean leftArmBusy = isArmBusy(model.leftArmPose, model.rightArmPose);
			this.DATA.updateHandPreferenceAndRelativeHeadYaw(rightArmBusy, leftArmBusy, model.head.yaw - model.body.yaw);
			AdvancedPosture thisFramePosture = AdvancedPosture.from(model);

			thisFramePosture.store(AdvancedArrangement.BEFORE_CFA_ANIMATIONS);

			if(this.prevFramePosture == null) // measure to prevent NPE; don't rely on this logic for animating!
				this.prevFramePosture = thisFramePosture;

			long worldTime = this.PLAYER.getWorld().getTime();
			boolean isFirstOfTick = worldTime > this.prevPosturingTick;
			if(isFirstOfTick) {
				this.prevPosturingTick = worldTime;
			}

			// If we are transitioning into, out of, or between animations, then we MUST interpolate for 1 tick!
			boolean forceWrappedInterpolation = worldTime <= this.forceInterpolationTime;
			boolean hasInterpolated;
			ActiveAnimation currentAnimation = this.getCurrentAnimation();
			if(currentAnimation != null) {
				if(currentAnimation instanceof ActiveAnimation.Interpolated) {
					// If the current animation is interpolating, then it'll also re-process visual counter-rotation as
					// a part of its interpolation logic.
					thisFramePosture.HEAD.pitch = this.originalHeadPitch;
					thisFramePosture.HEAD.yaw = this.originalHeadYaw;
				}
				hasInterpolated = !currentAnimation.ANIMATION.FLAGS.contains(AnimationFlag.NOT_INTERPOLATED);
				currentAnimation.mutatePosture(thisFramePosture, worldTime, tickDelta, isFirstOfTick, forceWrappedInterpolation);
			}
			else hasInterpolated = false;

			if(!hasInterpolated && forceWrappedInterpolation) {
				// Interpolate from last tick's thisFramePosture, to the CURRENTLY CALCULATED thisFramePosture.
				AdvancedPosture lerpTo = AdvancedPosture.from(thisFramePosture);
				thisFramePosture.wrappedLerp(tickDelta, this.prevFramePosture, lerpTo);
			}

			thisFramePosture.scaleTranslations(this.DATA.getHorizontalAnimationScale(), this.DATA.getVerticalAnimationScale());

			if(rightArmBusy) handleBusyArm(thisFramePosture.RIGHT_ARM, thisFramePosture.TORSO);
			if(leftArmBusy) handleBusyArm(thisFramePosture.LEFT_ARM, thisFramePosture.TORSO);

			thisFramePosture.apply(model);

//			model.body.zScale = 3;

			this.prevFramePosture = thisFramePosture;
		}
		catch(Throwable error) {
			CrashReport report = CrashReport.create(error, "Animating CFA Appearance");
			CrashReportSection appearanceSection = report.addElement("Player's model");
			appearanceSection.add("Current appearance: ", this.getAppearance(true).ID);
			appearanceSection.add("Current character: ", this.DATA.getCharacterID());
			appearanceSection.add("Current form: ", this.DATA.getFormID());
			appearanceSection.add("Current action: ", this.DATA.getActionID());
			throw new CrashException(report);
		}
	}

	private static void handleBusyArm(Arrangement arm, Arrangement torso) {
		AdvancedArrangement advArm = (AdvancedArrangement) arm;
		AdvancedArrangement advTorso = (AdvancedArrangement) torso;

		// FIRST: Calculate how vanilla wanted this arm to be positioned relative to the body.
		// We don't need to bother storing or restoring the arm's post-animation state since we'll be totally choosing
		// its position ourselves later anyways, and we WANT to keep its pre-animation angles.
		advTorso.store(AdvancedArrangement.AFTER_CFA_ANIMATIONS);
		advArm.resetTo(AdvancedArrangement.BEFORE_CFA_ANIMATIONS);
		advTorso.resetTo(AdvancedArrangement.BEFORE_CFA_ANIMATIONS);
		Vector3f vanillaPreferredDelta = new Vector3f(arm.x - torso.x, arm.y - torso.y, arm.z - torso.z);

		// SECOND: Put the torso back where the ActiveAnimation wanted it to be.
		advTorso.resetTo(AdvancedArrangement.AFTER_CFA_ANIMATIONS);

		// THIRD: Position the arm relative to the torso's animated position, maintaining the delta that vanilla wanted.
		arm.setPos(torso.x + vanillaPreferredDelta.x, torso.y + vanillaPreferredDelta.y, torso.z + vanillaPreferredDelta.z);

		// Arm's angles are already set properly due to advArm.resetTo earlier when calculating vanilla's delta! <3
	}

	private void animateFirstPerson(PlayerEntityModel<?> model, float tickDelta) {
		Hand rightHand = this.PLAYER.getMainArm() == Arm.RIGHT ? Hand.MAIN_HAND : Hand.OFF_HAND;
		Hand leftHand = rightHand == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
		boolean mapInBothHands = this.PLAYER.getMainHandStack().isOf(Items.FILLED_MAP)
				&& this.PLAYER.getOffHandStack().isEmpty();
		boolean itemInRightHand = mapInBothHands || !this.PLAYER.getStackInHand(rightHand).isEmpty();
		boolean itemInLeftHand = mapInBothHands || !this.PLAYER.getStackInHand(leftHand).isEmpty();

		model.rightArm.pitch = 0;
		model.leftArm.pitch = 0;

		// If I end up deciding to allow Actions to animate the first person hand, then that will go here.
		// That sounds like kind of a nightmare though. ^^;

		ParsedClientAppearance appearance = this.getAppearance(true);
		if(appearance != null) {
			TransformationInstructions empty = appearance.FP_EMPTY_HAND_TRANSFORMATION;
			TransformationInstructions filled = appearance.FP_FILLED_HAND_TRANSFORMATION;
			offsetArm(model.rightArm, 1, itemInRightHand ? filled : empty);
			offsetArm(model.leftArm, -1, itemInLeftHand ? filled : empty);
		}
	}

	private static void offsetArm(ModelPart arm, int factor, TransformationInstructions instructions) {
		arm.pivotX += factor * instructions.rightwards(); arm.pivotY += instructions.upwards(); arm.pivotZ += instructions.forwards();
		arm.pitch += instructions.pitch(); arm.yaw += instructions.yaw(); arm.roll += instructions.roll();
		arm.xScale *= instructions.xScale(); arm.yScale *= instructions.yScale(); arm.zScale *= instructions.zScale();
	}

	private static boolean isArmBusy(BipedEntityModel.ArmPose thisArmPose, BipedEntityModel.ArmPose otherArmPose) {
		return thisArmPose.isTwoHanded() || otherArmPose.isTwoHanded() || switch (thisArmPose) {
			case EMPTY, ITEM -> false;
			default -> true;
		};
	}
}
