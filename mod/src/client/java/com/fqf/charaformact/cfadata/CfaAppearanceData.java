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
import com.fqf.charaformact_api.definitions.states.actions.util.animation.Arrangement;
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

import static net.minecraft.util.math.MathHelper.*;
import static net.minecraft.util.math.MathHelper.HALF_PI;

public class CfaAppearanceData<CfaDataType extends CfaPlayerData & CfaAnimatingData & CfaClientDataImpl> {
	public final AbstractClientPlayerEntity PLAYER;
	public final CfaDataType DATA;

	private @Nullable ParsedClientAppearance appearance, flickerModel;
	private @Nullable PlayerEntityRenderer renderer, flickerRenderer;

	public @Nullable ActiveAnimation actionAnimation;
	public @Nullable ActiveAnimation forcedAnimation;
	private float forcedAnimationDuration;

	private long prevTick;
	private AdvancedPosture prevTickPosture;
	private long forceInterpolationTime;
	public Arrangement everythingArrangement;

	private long flickerUntil;
	private boolean flickering;

	public CfaAppearanceData(CfaDataType data) {
		this.PLAYER = data.getPlayer();
		this.DATA = data;

		this.flickerUntil = Long.MIN_VALUE;
		this.everythingArrangement = new AdvancedArrangement();
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
		ActiveAnimation newActionAnim = ActiveAnimation.of(this, this.DATA.getAction().ANIMATION, this.prevTickPosture);
		boolean forceInterpolation = newActionAnim != null || actionAnimation != null;
		this.actionAnimation = newActionAnim;
		if(forceInterpolation) this.forceInterpolation();
	}

	public void triggerAnimation(@NotNull AnimationDefinition definition, float duration) {
		this.forcedAnimation = ActiveAnimation.of(this, new ParsedAnimation(definition), this.prevTickPosture);
		this.forcedAnimationDuration = duration;
		this.forceInterpolation();
	}

	public @Nullable ActiveAnimation getCurrentAnimation() {
		if(this.forcedAnimation != null) return this.forcedAnimation;
		else return this.actionAnimation;
	}

	private void forceInterpolation() {
		this.forceInterpolationTime = this.PLAYER.getWorld().getTime() + 1;
	}

	public void animate(PlayerEntityModel<?> model, float tickDelta) {
		boolean rightArmBusy = isArmBusy(model.rightArmPose, model.leftArmPose);
		boolean leftArmBusy = isArmBusy(model.leftArmPose, model.rightArmPose);
		this.DATA.updateHandPreference(rightArmBusy, leftArmBusy);
		AdvancedPosture thisFramePosture = AdvancedPosture.from(model);

		if(rightArmBusy) ((AdvancedArrangement) thisFramePosture.RIGHT_ARM).store(AdvancedArrangement.BUSY_ARMS_SLOT);
		if(leftArmBusy) ((AdvancedArrangement) thisFramePosture.LEFT_ARM).store(AdvancedArrangement.BUSY_ARMS_SLOT);

		if(this.prevTickPosture == null) // measure to prevent NPE; don't rely on this logic for animating!
			this.prevTickPosture = thisFramePosture;

		long worldTime = this.PLAYER.getWorld().getTime();
		boolean isFirstOfTick = worldTime > this.prevTick;
		if(isFirstOfTick) {
			this.prevTick = worldTime;
		}

		try {
			boolean hasInterpolated;
			ActiveAnimation currentAnimation = this.getCurrentAnimation();
			if(currentAnimation != null) {
				hasInterpolated = currentAnimation.ANIMATION.FLAGS.contains(AnimationFlag.NOT_INTERPOLATED);
				currentAnimation.apply(thisFramePosture, worldTime, tickDelta, isFirstOfTick);
			}
			else hasInterpolated = false;

			if(!hasInterpolated && worldTime <= this.forceInterpolationTime) {
				// Interpolate from last tick's thisFramePosture, to the CURRENTLY CALCULATED thisFramePosture.
				AdvancedPosture lerpTo = AdvancedPosture.from(thisFramePosture);
				thisFramePosture.lerp(this.prevTickPosture, lerpTo, tickDelta);
			}

			if(rightArmBusy) ((AdvancedArrangement) thisFramePosture.RIGHT_ARM).resetTo(AdvancedArrangement.BUSY_ARMS_SLOT);
			if(leftArmBusy) ((AdvancedArrangement) thisFramePosture.LEFT_ARM).resetTo(AdvancedArrangement.BUSY_ARMS_SLOT);

			thisFramePosture.apply(model);
			this.everythingArrangement = thisFramePosture.EVERYTHING;
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

		if(worldTime > this.forceInterpolationTime) this.prevTickPosture = thisFramePosture;

//		if(this.forcedAnimation != null)
	}

	public void rotateTotalRender(MatrixStack matrices) {
		matrices.translate(this.everythingArrangement.x / 16F, this.everythingArrangement.y / 16F, this.everythingArrangement.z / 16F);
		matrices.multiply(RotationAxis.POSITIVE_Y.rotation(this.everythingArrangement.yaw));
		double halfHeight = this.PLAYER.getHeight() * 0.5F;
		matrices.translate(0, halfHeight, 0);
		matrices.multiply(RotationAxis.POSITIVE_X.rotation(this.everythingArrangement.pitch));
		matrices.multiply(RotationAxis.POSITIVE_Z.rotation(this.everythingArrangement.roll));
		matrices.translate(0, -halfHeight, 0);
	}

	private static final float ALMOST_HALF_PI = 0.99F * HALF_PI;
	private static final float MAX_HEAD_YAW = HALF_PI * 0.66F;
	public float counterRotateHead(
			ModelPart head, float assigningPitch
	) {
		head.yaw = MathHelper.clamp(wrapRadians(head.yaw + this.everythingArrangement.yaw), -MAX_HEAD_YAW, MAX_HEAD_YAW);
		return MathHelper.clamp(wrapRadians(assigningPitch + this.everythingArrangement.pitch), -ALMOST_HALF_PI, ALMOST_HALF_PI);
	}

	private static float wrapRadians(float radians) {
		float wrapped = radians % TAU;
		if(wrapped >= PI) wrapped -= TAU;
		if(wrapped < -PI) wrapped += TAU;
		return wrapped;
	}

	private static boolean isArmBusy(BipedEntityModel.ArmPose thisArmPose, BipedEntityModel.ArmPose otherArmPose) {
		return thisArmPose.isTwoHanded() || otherArmPose.isTwoHanded() || switch (thisArmPose) {
			case EMPTY, ITEM -> false;
			default -> true;
		};
	}
}
