package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.Easing;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class WalkRun extends SubWalk implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("walk_run");
	@Override public @NotNull Identifier defineID() {
	    return ID;
	}

	private static float getProgress(CfaAnimatingData data) {
		return Easing.clampedRangeToProgress(data.getForwardVel(), SubWalk.WALK_SPEED.get(data), RUN_SPEED.get(data));
	}

	@Override public @Nullable AnimationDefinition getAnimation() {
		return AnimationDefinition.of(
				null,
				(arrangement, data, animationTime, helper) ->
						arrangement.roll = MathHelper.clamp((float) data.getDeltaYaw() * getProgress(data) * -4F, -45F, 45F),
				(posture, data, animationTime, helper) -> {
					float progress = getProgress(data);
					helper.symmetricallyAnimate(posture, posture.RIGHT_ARM, arrangement ->
							arrangement.roll += progress * 70);
				}
		);
	}

	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.ALLOW;
	}

	public static final CfaStat RUN_ACCEL = new CfaStat(0.0175, RUNNING, FORWARD, ACCELERATION);
	public static final CfaStat RUN_SPEED = new CfaStat(0.55, RUNNING, FORWARD, SPEED);
	public static final CfaStat RUN_REDIRECTION = new CfaStat(2.76, RUNNING, FORWARD, REDIRECTION);
	public static final CfaStat OVERRUN_ACCEL = new CfaStat(0.0175, RUNNING, FORWARD, OVERSPEED_CORRECTION);

	public static boolean meetsWalkRunRequirement(CfaReadableMotionData data) {
		return data.getInputs().getForwardInput() > 0 &&
				data.getForwardVel() > WALK_STANDSTILL_THRESHOLD.get(data) &&
				data.getHorizVelSquared() > WALK_SPEED.getAsSquaredThreshold(data);
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new ActionTimerVars();
	}
	@Override public void travelHook(CfaTravelData data, GroundedActionHelper helper) {
		if(data.getPlayer().isSprinting()) {
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
						SubWalk.ID,
						data -> !meetsWalkRunRequirement(data),
						EvaluatorEnvironment.CLIENT_ONLY
				),
				new TransitionDefinition(
						PRun.ID,
						data -> PRun.meetsPRunRequirements(data) && ActionTimerVars.get(data).actionTimer > 20,
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of(
				new TransitionInjectionDefinition(
						TransitionInjectionDefinition.InjectionPlacement.BEFORE,
						SubWalk.ID,
						ActionCategory.AIRBORNE,
						(nearbyTransition, castableHelper) -> nearbyTransition.variate(
								this.defineID(),
								data -> (data.isServer() || meetsWalkRunRequirement(data)) && nearbyTransition.evaluator().shouldTransition(data),
								EvaluatorEnvironment.CLIENT_CHECKED, null, null
						)
				),
				new TransitionInjectionDefinition(
						TransitionInjectionDefinition.InjectionPlacement.BEFORE,
						SubWalk.ID,
						ActionCategory.GROUNDED,
						(nearbyTransition, castableHelper) -> nearbyTransition.variate(
								this.defineID(),
								data -> meetsWalkRunRequirement(data) && nearbyTransition.evaluator().shouldTransition(data),
								EvaluatorEnvironment.CLIENT_ONLY, null, null
						)
				)
		);
	}
}
