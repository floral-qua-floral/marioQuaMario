package com.fqf.mario_qua_mario_content.actions.power;

import com.fqf.mario_qua_mario_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.airborne.DuckFall;
import com.fqf.mario_qua_mario_content.actions.airborne.Fall;
import com.fqf.mario_qua_mario_content.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario_content.actions.grounded.DuckWaddle;
import com.fqf.mario_qua_mario_content.util.Powers;
import com.fqf.mario_qua_mario_content.util.TailSpinActionTimerVars;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario_api.util.StatCategory.*;

public class TailSpinFall implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("tail_spin_fall");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return TailSpinGround.ANIMATION;
	}

	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return TailSpinGround.CAMERA_ANIMATIONS;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.ALLOW;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
	return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable BumpType getBumpType() {
		return null;
	}
	@Override public @Nullable Identifier getStompTypeID() {
		return null;
	}

	public static final CharaStat FALL_ACCEL = Fall.FALL_ACCEL.variate(0.575, DUCKING, NORMAL_GRAVITY, POWER_UP);
	public static final CharaStat FALL_SPEED = Fall.FALL_SPEED.variate(0.6, DUCKING, TERMINAL_VELOCITY, POWER_UP);

	public static CharaStat REDUCED_FORWARD_ACCEL = Fall.FORWARD_DRIFT_ACCEL.variate(0.5);
	public static CharaStat REDUCED_BACKWARD_ACCEL = Fall.BACKWARD_DRIFT_ACCEL.variate(0.5);
	public static CharaStat REDUCED_STRAFE_ACCEL = Fall.STRAFE_DRIFT_ACCEL.variate(0.5);

	public static CharaStat REDUCED_REDIRECTION = Fall.DRIFT_REDIRECTION.variate(0.66);

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new TailSpinActionTimerVars(data);
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {
		TailSpinGround.commonTick(data);
	}
	@Override public void serverTick(IMarioAuthoritativeData data) {
		TailSpinGround.commonTick(data);
		TailSpinGround.attemptTailStrike(data);
	}
	@Override public void travelHook(IMarioTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, FALL_ACCEL, null, FALL_SPEED);
		helper.airborneAccel(data,
				REDUCED_FORWARD_ACCEL, Fall.FORWARD_DRIFT_SPEED,
				REDUCED_BACKWARD_ACCEL, Fall.BACKWARD_DRIFT_SPEED,
				REDUCED_STRAFE_ACCEL, Fall.STRAFE_DRIFT_SPEED,
				data.getForwardVel(), data.getStrafeVel(), REDUCED_REDIRECTION
		);
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AirborneActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						DuckFall.ID,
						data -> !data.hasPower(Powers.TAIL_ATTACK),
						EvaluatorEnvironment.COMMON
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
		return List.of(
				DuckWaddle.UNDUCK.variate(Fall.ID, null)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper) {
		return List.of(
				Submerged.SUBMERGE,
				Fall.LANDING.variate(TailSpinGround.ID,
						data -> !TailSpinGround.doneSpinning(data) && Fall.LANDING.evaluator().shouldTransition(data)),
				Fall.LANDING.variate(DuckWaddle.ID, null)
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
