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
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.GroundPoundDrop;
import com.fqf.mario_qua_mario.actions.aquatic.AquaticPoundLand;
import com.fqf.mario_qua_mario.actions.aquatic.UnderwaterWalk;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.StandUpWithKneeAnimation;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class GroundPoundLand implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("ground_pound_land");
	@Override public @NotNull Identifier defineID() {
	    return ID;
	}

	private static final float STANDUP_TICKS = 10;

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return BonkGroundBackward.makeAnimation(StandUpWithKneeAnimation.makeProgressCalculator(STANDUP_TICKS));
	}

	@Override public @NotNull SlidingStatus defineSlidingStatus() {
		return SlidingStatus.SLIDING_SILENT;
	}

	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new ActionTimerVars();
	}
	@Override public void travelHook(CfaTravelData data, GroundedActionHelper helper) {
		data.setForwardStrafeVel(0, 0);
		data.retrieveStateData(ActionTimerVars.class).actionTimer++;
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<TransitionDefinition> builder, GroundedActionHelper helper) {
		builder.add(new TransitionDefinition(
				SubWalk.ID,
				data -> ActionTimerVars.get(data).actionTimer > STANDUP_TICKS,
				EvaluatorEnvironment.CLIENT_ONLY
		));
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<TransitionDefinition> builder, GroundedActionHelper helper) {
		builder.add(
				Fall.FALL.variate(
						GroundPoundDrop.ID,
						data -> data.getInputs().DUCK.isHeld() && Fall.FALL.evaluator().shouldTransition(data)
				),
				Fall.FALL,
				UnderwaterWalk.SUBMERGE.variate(AquaticPoundLand.ID, null)
		);
	}
}