package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.CharaStat;
import net.minecraft.util.Identifier;
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

	@Override public void travelHook(IMarioTravelData data, GroundedActionHelper helper) {
		if(data.getMario().isSprinting()) {
			if(data.getForwardVel() > RUN_SPEED.getAsLimit(data)) {
				// Overrun
				helper.groundAccel(data,
						OVERRUN_ACCEL, RUN_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						data.getInputs().getForwardInput(), data.getInputs().getStrafeInput() * 0.8,
						RUN_REDIRECTION
				);
			}
			else {
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
				new TransitionDefinition(
						MarioQuaMarioContent.makeID("sub_walk"),
						data -> !meetsWalkRunRequirement(data),
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of(
				new TransitionInjectionDefinition(
						TransitionInjectionDefinition.InjectionPlacement.BEFORE,
						MarioQuaMarioContent.makeID("sub_walk"),
						TransitionInjectionDefinition.ActionCategory.AIRBORNE,
						nearbyTransition -> new TransitionDefinition(
								this.getID(),
								data -> meetsWalkRunRequirement(data) && nearbyTransition.evaluator().shouldTransition(data),
								EvaluatorEnvironment.CLIENT_ONLY,
								nearbyTransition.travelExecutor(),
								nearbyTransition.clientsExecutor()
						)
				)
		);
	}
}
