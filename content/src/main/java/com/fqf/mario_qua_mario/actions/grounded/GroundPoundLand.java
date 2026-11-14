package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.mario_qua_mario.CfaMarioAddon;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.GroundPoundDrop;
import com.fqf.mario_qua_mario.actions.aquatic.AquaticPoundLand;
import com.fqf.mario_qua_mario.actions.aquatic.UnderwaterWalk;
import com.fqf.mario_qua_mario.util.StandUpWithKneeAnimation;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class GroundPoundLand implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("ground_pound_land");

	private static final float STANDUP_TICKS = 10;

	@Override
	public float defineHitboxEyeHeight() {
		return Math.min(GroundedActionDefinition.super.defineHitboxEyeHeight(), this.defineHitboxHeight() / 2 + 0.2F);
	}

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return BonkGround.makeAnimation(StandUpWithKneeAnimation.makeProgressCalculator(STANDUP_TICKS));
	}

	@Override
	public void defineActionFlags(ImmutableSet.Builder<ActionFlag> builder) {
		builder.addAll(ActionFlag.SLIDING);
		builder.add(ActionFlag.CROUCHING, ActionFlag.PROHIBIT_SPRINT);
	}

	@Override public void travelHook(CfaTravelData data, GroundedActionHelper helper) {
		data.setForwardStrafeVel(0, 0);
		data.get(CfaMarioAddon.ACTION_TIMER).increment();
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(new ActionTransitionDetails(
				SubWalk.ID,
				data -> data.get(CfaMarioAddon.ACTION_TIMER).intValue() > STANDUP_TICKS,
				EvaluatorEnvironment.CLIENT_ONLY
		));
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(
				Fall.FALL.variate(
						GroundPoundDrop.ID,
						data -> data.getInputs().DUCK.isHeld() && Fall.FALL.evaluator().test(data)
				),
				Fall.FALL,
				UnderwaterWalk.SUBMERGE.variate(AquaticPoundLand.ID, null)
		);
	}
}