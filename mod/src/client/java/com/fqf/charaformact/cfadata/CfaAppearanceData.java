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
import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Pair;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.util.math.MathHelper.HALF_PI;

public class CfaAppearanceData<CfaDataType extends CfaPlayerData & CfaAnimatingData & CfaClientDataImpl> {
	public final AbstractClientPlayerEntity PLAYER;
	public final CfaDataType DATA;

	private @Nullable ParsedClientAppearance appearance, flickerModel;
	private @Nullable PlayerEntityRenderer renderer, flickerRenderer;

	public @Nullable ActiveAnimation actionAnimation;
	public @Nullable ActiveAnimation forcedAnimation;
	private float forcedAnimationDuration;

	private long forceInterpolationTime;

	private long prevPosturingTick;
	private AdvancedPosture prevFramePosture;

	private long prevArrangingTick;
	private AdvancedArrangement prevFrameModelArrangement;
	private AdvancedArrangement thisFrameModelArrangement;

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
	public void triggerAnimation(@NotNull AnimationDefinition definition, float duration) {
		this.forcedAnimation = ActiveAnimation.of(this, new ParsedAnimation(definition),
				this.prevFramePosture, this.prevFrameModelArrangement);
		this.forcedAnimationDuration = duration;
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

		this.prevFrameModelArrangement = this.thisFrameModelArrangement;

		matrices.translate(this.thisFrameModelArrangement.x / 16F, this.thisFrameModelArrangement.y / 16F, this.thisFrameModelArrangement.z / 16F);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotation(this.thisFrameModelArrangement.yaw));
		double halfHeight = this.PLAYER.getHeight() * 0.5F;
		matrices.translate(0, halfHeight, 0);
		matrices.multiply(RotationAxis.POSITIVE_X.rotation(this.thisFrameModelArrangement.pitch));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotation(this.thisFrameModelArrangement.roll));
		matrices.translate(0, -halfHeight, 0);
	}

	private float originalHeadPitch, originalHeadYaw;
	private static final float ALMOST_HALF_PI = 0.99F * HALF_PI;
	private static final float MAX_HEAD_YAW = HALF_PI * 0.66F;
	public float counterRotateHead(ModelPart head, float assigningPitch) {
		this.originalHeadPitch = head.pitch;
		this.originalHeadYaw = head.yaw;

		head.yaw = MathHelper.clamp(AdvancedArrangement.wrapRadians(head.yaw + this.thisFrameModelArrangement.yaw), -MAX_HEAD_YAW, MAX_HEAD_YAW);
		return MathHelper.clamp(AdvancedArrangement.wrapRadians(assigningPitch + this.thisFrameModelArrangement.pitch), -ALMOST_HALF_PI, ALMOST_HALF_PI);
	}

	public void animate(PlayerEntityModel<?> model, float tickDelta) {
		try {
			// We have to undo head counter-rotation then redo it later, otherwise the counter-rotation will be
			// mucked around with a little by Posture lerping, which will desynchronize it from playermodel arrangement.
			float headPitchAdjustment = model.head.pitch - this.originalHeadPitch;
			float headYawAdjustment = model.head.yaw - this.originalHeadYaw;
			model.head.pitch = this.originalHeadPitch;
			model.head.yaw = this.originalHeadYaw;

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
				hasInterpolated = !currentAnimation.ANIMATION.FLAGS.contains(AnimationFlag.NOT_INTERPOLATED);
				currentAnimation.mutatePosture(thisFramePosture, worldTime, tickDelta, isFirstOfTick, forceWrappedInterpolation);
			}
			else hasInterpolated = false;

			if(!hasInterpolated && forceWrappedInterpolation) {
				// Interpolate from last tick's thisFramePosture, to the CURRENTLY CALCULATED thisFramePosture.
				AdvancedPosture lerpTo = AdvancedPosture.from(thisFramePosture);
				thisFramePosture.wrappedLerp(tickDelta, this.prevFramePosture, lerpTo);
			}

			if(rightArmBusy) ((AdvancedArrangement) thisFramePosture.RIGHT_ARM).resetTo(AdvancedArrangement.BEFORE_CFA_ANIMATIONS);
			if(leftArmBusy) ((AdvancedArrangement) thisFramePosture.LEFT_ARM).resetTo(AdvancedArrangement.BEFORE_CFA_ANIMATIONS);

			float horizontalScale = this.DATA.getCharacter().ANIMATION_HORIZONTAL_SCALE * this.DATA.getForm().ANIMATION_HORIZONTAL_SCALE;
			float verticalScale = this.DATA.getCharacter().ANIMATION_VERTICAL_SCALE * this.DATA.getForm().ANIMATION_VERTICAL_SCALE;
			thisFramePosture.scaleTranslations(horizontalScale, verticalScale);

			thisFramePosture.apply(model);

			model.head.pitch += headPitchAdjustment;
			model.head.yaw += headYawAdjustment;

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

	private static boolean isArmBusy(BipedEntityModel.ArmPose thisArmPose, BipedEntityModel.ArmPose otherArmPose) {
		return thisArmPose.isTwoHanded() || otherArmPose.isTwoHanded() || switch (thisArmPose) {
			case EMPTY, ITEM -> false;
			default -> true;
		};
	}
}
