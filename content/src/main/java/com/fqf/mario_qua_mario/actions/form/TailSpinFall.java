package com.fqf.mario_qua_mario.actions.form;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.DuckFall;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario.actions.grounded.DuckWaddle;
import com.fqf.mario_qua_mario.util.Powers;
import com.fqf.mario_qua_mario.util.TailSpinActionTimerVars;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class TailSpinFall implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("tail_spin_fall");
	@Override public @NotNull Identifier defineID() {
	    return ID;
	}

	@Override
	public @Nullable AnimationDefinition defineAnimation() {
		return TailSpinGround.makeAnimation(false);
	}

	@Override public @Nullable CameraAnimationSet defineCameraAnimations(AnimationHelper helper) {
		return TailSpinGround.CAMERA_ANIMATIONS;
	}

	@Override public @NotNull SprintingRule defineSprintingRule() {
	return SprintingRule.PROHIBIT;
	}

	public static final CfaStat FALL_ACCEL = Fall.FALL_ACCEL.variateAndReplaceCategories(0.575, DUCKING, NORMAL_GRAVITY, FORM);
	public static final CfaStat FALL_SPEED = Fall.FALL_SPEED.variateAndReplaceCategories(0.6, DUCKING, TERMINAL_VELOCITY, FORM);

	public static CfaStat REDUCED_FORWARD_ACCEL = Fall.FORWARD_DRIFT_ACCEL.variate(0.5);
	public static CfaStat REDUCED_BACKWARD_ACCEL = Fall.BACKWARD_DRIFT_ACCEL.variate(0.5);
	public static CfaStat REDUCED_STRAFE_ACCEL = Fall.STRAFE_DRIFT_ACCEL.variate(0.5);

	public static CfaStat REDUCED_REDIRECTION = Fall.DRIFT_REDIRECTION.variate(0.66);

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new TailSpinActionTimerVars(data);
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {
		TailSpinGround.commonTick(data);
	}
	@Override public void serverTick(CfaAuthoritativeData data) {
		TailSpinGround.commonTick(data);
		TailSpinGround.attemptTailStrike(data);
	}
	@Override public void travelHook(CfaTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, FALL_ACCEL, null, FALL_SPEED);
		helper.airborneAccel(data,
				REDUCED_FORWARD_ACCEL, Fall.FORWARD_DRIFT_SPEED,
				REDUCED_BACKWARD_ACCEL, Fall.BACKWARD_DRIFT_SPEED,
				REDUCED_STRAFE_ACCEL, Fall.STRAFE_DRIFT_SPEED,
				data.getForwardVel(), data.getStrafeVel(), REDUCED_REDIRECTION
		);
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {
		builder.add(
				DuckWaddle.UNDUCK.variate(Fall.ID, null),
				new ActionTransitionDetails(
						DuckFall.ID,
						data -> !data.hasPower(Powers.TAIL_ATTACK),
						EvaluatorEnvironment.COMMON
				)
		);
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {
		builder.add(
				Submerged.SUBMERGE,
				Fall.LANDING.variate(TailSpinGround.ID,
						data -> !TailSpinGround.doneSpinning(data) && Fall.LANDING.evaluator().shouldTransition(data)),
				Fall.LANDING.variate(DuckWaddle.ID, null)
		);
	}
}
