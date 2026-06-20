package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.Easing;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.BonkAir;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.aquatic.UnderwaterWalk;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.StandUpWithKneeAnimation;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.DRAG;
import static com.fqf.charaformact_api.util.StatCategory.RUNNING;

public class BonkGroundBackward implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("bonk_ground_backward");
	@Override public @NotNull Identifier defineID() {
		return ID;
	}

	public static final float STANDUP_TICKS = 8;
	protected static final StandUpWithKneeAnimation.ProgressCalculator PROGRESS_CALCULATOR = (data, animationTime) ->
			2 * Easing.SINE_IN_OUT.ease(Math.min(1, data.retrieveStateData(ActionTimerVars.class).actionTimer / STANDUP_TICKS));

	public static AnimationDefinition makeAnimation(StandUpWithKneeAnimation.ProgressCalculator progressCalculator) {
		return StandUpWithKneeAnimation.makeAnimation(
				progressCalculator,
				1.75F, 10,
				22.5F, -20, 0, 2,
				-90, 5, 1.5F,
				-79, 10, -1.55F, 3
		);
	}

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return makeAnimation(PROGRESS_CALCULATOR);
	}

	@Override public @NotNull SlidingStatus defineSlidingStatus() {
		return SlidingStatus.SLIDING;
	}

	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	public static final CfaStat BONK_DRAG = new CfaStat(0.185, RUNNING, DRAG);
	public static final CfaStat BONK_DRAG_MIN = new CfaStat(0.045, RUNNING, DRAG);

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new ActionTimerVars();
	}
	@Override public void travelHook(CfaTravelData data, GroundedActionHelper helper) {
		if(data.getHorizVelSquared() == 0) data.retrieveStateData(ActionTimerVars.class).actionTimer++;
		else data.retrieveStateData(ActionTimerVars.class).actionTimer = 0;
		helper.applyDrag(
				data, BONK_DRAG, BONK_DRAG_MIN,
				-data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				CfaStat.ZERO
		);
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<TransitionDefinition> builder, GroundedActionHelper helper) {
		builder.add(new TransitionDefinition(
				SubWalk.ID,
				data -> data.retrieveStateData(ActionTimerVars.class).actionTimer > STANDUP_TICKS,
				EvaluatorEnvironment.CLIENT_ONLY
		));
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<TransitionDefinition> builder, GroundedActionHelper helper) {
		builder.add(
				Fall.FALL.variate(
						BonkAir.ID,
						data -> Fall.FALL.evaluator().shouldTransition(data) && data.retrieveStateData(ActionTimerVars.class).actionTimer == 0,
						EvaluatorEnvironment.CLIENT_ONLY,
						null,
						null
				),
				Fall.FALL,
				UnderwaterWalk.SUBMERGE
		);
	}
}
