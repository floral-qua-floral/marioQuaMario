package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario.mariodata.*;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.Easing;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario.util.StatCategory.*;
import static com.fqf.mario_qua_mario.util.StatCategory.OVERSPEED_CORRECTION;

public class WalkRun extends SubWalk implements GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("walk_run");
	}

	@Override
	public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null,
				new ProgressHandler(
						(data, ticksPassed) ->
								Easing.clampedRangeToProgress(data.getForwardVel(), SubWalk.WALK_SPEED.get(data), RUN_SPEED.get(data))
				),

				new EntireBodyAnimation(0.0F, (data, arrangement, progress) ->
						arrangement.roll = MathHelper.clamp((float) data.getDeltaYaw() * progress * -4F, -45F, 45F)),
				null, null,

				new LimbAnimation(true, (data, arrangement, progress) -> arrangement.roll += progress * 70),
				new LimbAnimation(true, (data, arrangement, progress) -> arrangement.roll -= progress * 70),

				null, null, null
		);
	}

	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.ALLOW;
	}

	public static final CharaStat RUN_ACCEL = new CharaStat(0.0175, RUNNING, FORWARD, ACCELERATION);
	public static final CharaStat RUN_SPEED = new CharaStat(0.55, RUNNING, FORWARD, SPEED);
	public static final CharaStat RUN_REDIRECTION = new CharaStat(2.76, RUNNING, FORWARD, REDIRECTION);
	public static final CharaStat OVERRUN_ACCEL = new CharaStat(0.0175, RUNNING, FORWARD, OVERSPEED_CORRECTION);

	public static boolean meetsWalkRunRequirement(IMarioReadableMotionData data) {
		return data.getInputs().getForwardInput() > 0 &&
				data.getForwardVel() > WALK_STANDSTILL_THRESHOLD.get(data) &&
				data.getHorizVelSquared() > WALK_SPEED.getAsSquaredThreshold(data);
	}

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new ActionTimerVars();
	}
	@Override public void travelHook(IMarioTravelData data, GroundedActionHelper helper) {
		if(data.getMario().isSprinting()) {
			ActionTimerVars vars = ActionTimerVars.get(data);

			if(data.getForwardVel() > RUN_SPEED.getAsLimit(data)) {
				if(Math.abs(data.getStrafeVel()) < 0.175) vars.actionTimer = 50;
				// Overrun
				helper.groundAccel(data,
						OVERRUN_ACCEL, RUN_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						data.getInputs().getForwardInput(), data.getInputs().getStrafeInput() * 0.8,
						RUN_REDIRECTION
				);
			}
			else {
				if(data.getForwardVel() > RUN_SPEED.getAsThreshold(data) && Math.abs(data.getStrafeVel()) < 0.175)
					vars.actionTimer++;
				else if(vars.actionTimer > 0) vars.actionTimer = 0;

				// Run Accel
				helper.groundAccel(data,
						RUN_ACCEL, RUN_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
						RUN_REDIRECTION
				);
			}
		}
		else super.travelHook(data, helper);
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper) {
		return List.of(
				DuckWaddle.DUCK,
				Skid.SKID,
				new TransitionDefinition(
						MarioQuaMarioContent.makeID("sub_walk"),
						data -> !meetsWalkRunRequirement(data),
						EvaluatorEnvironment.CLIENT_ONLY
				),
				new TransitionDefinition(
						MarioQuaMarioContent.makeID("p_run"),
						data -> PRun.meetsPRunRequirements(data) && ActionTimerVars.get(data).actionTimer > 20,
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of(
				new TransitionInjectionDefinition(
						TransitionInjectionDefinition.InjectionPlacement.BEFORE,
						MarioQuaMarioContent.makeID("sub_walk"),
						ActionCategory.AIRBORNE,
						(nearbyTransition, castableHelper) -> nearbyTransition.variate(
								this.getID(),
								data -> (data.isServer() || meetsWalkRunRequirement(data)) && nearbyTransition.evaluator().shouldTransition(data),
								EvaluatorEnvironment.CLIENT_CHECKED, null, null
						)
				),
				new TransitionInjectionDefinition(
						TransitionInjectionDefinition.InjectionPlacement.BEFORE,
						MarioQuaMarioContent.makeID("sub_walk"),
						ActionCategory.GROUNDED,
						(nearbyTransition, castableHelper) -> nearbyTransition.variate(
								this.getID(),
								data -> meetsWalkRunRequirement(data) && nearbyTransition.evaluator().shouldTransition(data),
								EvaluatorEnvironment.CLIENT_ONLY, null, null
						)
				)
		);
	}
}
