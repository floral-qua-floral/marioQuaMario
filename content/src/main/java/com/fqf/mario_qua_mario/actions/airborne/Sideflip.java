package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.cfadata.*;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraProgressHandler;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.BodyPartAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.EntireBodyAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.LimbAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.PiecemealPlayermodelAnimation;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.Easing;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag.*;
import static com.fqf.charaformact_api.util.StatCategory.*;

public class Sideflip extends Backflip implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("sideflip");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	private static float calculateProgress(float animationTime) {
		return Easing.LINEAR.ease(Math.min(animationTime / 20F, 1));
	}

	private static void conditionallyFlip(CfaAnimatingData data, Arrangement arrangement) {
		if(!data.retrieveStateData(SideflipVars.class).hasRotated) arrangement.yaw += 180;
	}

	@Override public @Nullable AnimationDefinition getAnimation() {
		return AnimationDefinition.of(
				EnumSet.of(NO_RIGHT_ARM_SWING, NO_LEFT_ARM_SWING, NO_RIGHT_LEG_SWING, NO_LEFT_LEG_SWING, NOT_INTERPOLATED, NO_HEAD_COUNTERROTATION),
				(arrangement, data, animationTime, helper) -> {
					float progress = calculateProgress(animationTime);
					arrangement.y -= MathHelper.lerp(progress, 4F, 0);
					arrangement.addAngles(
							0,
							MathHelper.lerp(progress, -136.5F, 0),
							Easing.mixedEase(Easing.LINEAR, Easing.QUAD_IN_OUT, progress, -342.5F, 0)
					);
					conditionallyFlip(data, arrangement);
				},
				(posture, data, animationTime, helper) -> {
					float progress = calculateProgress(animationTime);
					float fourProgress = progress * 4;

					posture.HEAD.addAngles(
							MathHelper.lerp(progress, 0, 0),
							MathHelper.lerp(progress, 42.5F, 0),
							MathHelper.lerp(progress, -15, 0)
					);

					posture.RIGHT_ARM.addAngles(
							helper.interpolateKeyframes(fourProgress, -32, -85, 10, 24, 12),
							helper.interpolateKeyframes(fourProgress, -35, 0, 47.5F, 20, 0),
							helper.interpolateKeyframes(fourProgress, 80, 90, 130, 130, 115)
					);
					posture.LEFT_ARM.addAngles(
							helper.interpolateKeyframes(fourProgress, -45, 30, 0, 0, -4),
							helper.interpolateKeyframes(fourProgress, 0, 20, -17.5F, -35, 0),
							helper.interpolateKeyframes(fourProgress, -30, -90, -115, -107.5F, -137.5F)
					);

					posture.RIGHT_LEG.addPos(
							0,
							helper.interpolateKeyframes(fourProgress, 0, 0, -4.1F, -1.2633F, 0),
							helper.interpolateKeyframes(fourProgress, 0, 0, -4.0F, -3.75F, 0)
					);
					posture.RIGHT_LEG.addAngles(
							helper.interpolateKeyframes(fourProgress, -57.5F, 40, 26, 38.5F, 20),
							helper.interpolateKeyframes(fourProgress, 45, 0, -7, -5, 0),
							helper.interpolateKeyframes(fourProgress, -20, -12.5F, 8, 10, 0)
					);

					posture.LEFT_LEG.addPos(
							0,
							helper.interpolateKeyframes(fourProgress, -4.1F, -1.5F, -0.8F, -1.85F, -3.9F),
							helper.interpolateKeyframes(fourProgress, -3.9F, -2.1F, -0.91F, -2.3F, -1.9F)
					);
					posture.LEFT_LEG.addAngles(
							helper.interpolateKeyframes(fourProgress, 5, 20, 5, 4, 0),
							helper.interpolateKeyframes(fourProgress, 15, 0, 0, 5, 0),
							helper.interpolateKeyframes(fourProgress, 0, -22.5F, -15, 0, 0)
					);
				}
		);
	}

	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return new CameraAnimationSet(
				MarioQuaMario.CONFIG::getSideflipCameraAnim,
				new CameraAnimation(
						new CameraProgressHandler((data, ticksPassed) -> Math.min(ticksPassed / 16, 1)),
						(data, arrangement, progress) -> {
							conditionallyFlip(data, arrangement);
							arrangement.yaw += MathHelper.lerp(Easing.SINE_IN_OUT.ease(progress), 180, 0);
							arrangement.roll += Easing.QUAD_IN_OUT.ease(progress) * -360;
						}
				),
				new CameraAnimation(
						new CameraProgressHandler((data, ticksPassed) -> Easing.QUAD_IN_OUT.ease(Math.min(ticksPassed / 14, 1))),
						(data, arrangement, progress) -> {
							conditionallyFlip(data, arrangement);
							arrangement.pitch = MathHelper.lerp(progress, -180 - arrangement.pitch, arrangement.pitch);
							arrangement.roll += MathHelper.lerp(progress, 180, 0);
						}
				),
				new CameraAnimation(
						new CameraProgressHandler((data, ticksPassed) -> Easing.SINE_IN_OUT.ease(Math.min(ticksPassed / 10, 1))),
						(data, arrangement, progress) -> {
							conditionallyFlip(data, arrangement);
							arrangement.yaw += MathHelper.lerp(progress, 180, 0);
						}
				)
		);
	}

	public static CfaStat SIDEFLIP_VEL = new CfaStat(1.065, JUMP_VELOCITY);
	public static CfaStat SIDEFLIP_BACKWARDS_SPEED = new CfaStat(-0.375, DRIFTING, BACKWARD, SPEED);
	public static CfaStat SIDEFLIP_THRESHOLD = new CfaStat(0.2, WALKING, FRICTION, THRESHOLD);

	private static class SideflipVars extends ActionTimerVars {
		public boolean hasRotated;
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		SideflipVars vars = data.retrieveStateData(SideflipVars.class);
		if(vars == null) vars = new SideflipVars();
		return vars;
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {
		SideflipVars sideflipVars = data.retrieveStateData(SideflipVars.class);
		if(!sideflipVars.hasRotated && sideflipVars.actionTimer++ >= 3) {
			sideflipVars.hasRotated = true;
			data.instantVisualRotate(180, false);
		}
	}
	@Override public void serverTick(CfaAuthoritativeData data) {

	}
	@Override public void travelHook(CfaTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, Fall.FALL_ACCEL, JUMP_GRAVITY, Fall.FALL_SPEED);
		if(data.getYVel() < 0.1) Fall.drift(data, helper);
	}

	@Override protected double getJumpCapThreshold() {
		return 0.65;
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}