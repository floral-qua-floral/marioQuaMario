package com.fqf.mario_qua_mario.actions.aquatic;

import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.fqf.mario_qua_mario.util.StandUpWithKneeAnimation;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AquaticPoundLand implements AquaticActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("aquatic_ground_pound_land");

	private static final float AQUATIC_STANDUP_TICKS = 15;

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return StandUpWithKneeAnimation.makeAnimation(
				StandUpWithKneeAnimation.makeProgressCalculator(AQUATIC_STANDUP_TICKS),
				1.75F, 10,
				22.5F, -20, 0, 2,
				-90, 5, 1.5F,
				-79, 10, -1.55F, 3,

				15, UnderwaterWalk.LEG_HEIGHT_OFFSET,
				-50, -10, 60,
				17.5F, -1.9F, -0.4F, UnderwaterWalk.LEG_HEIGHT_OFFSET, -0.9F
		);
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
	@Override public void travelHook(CfaTravelData data, AquaticActionHelper helper) {
		Submerged.waterMove(data, helper);
		data.setForwardStrafeVel(0, 0);
		data.retrieveStateData(ActionTimerVars.class).actionTimer++;
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AquaticActionHelper helper) {
		builder.add(new ActionTransitionDetails(
				UnderwaterWalk.ID,
				data -> data.retrieveStateData(ActionTimerVars.class).actionTimer > AQUATIC_STANDUP_TICKS,
				EvaluatorEnvironment.COMMON
		));
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AquaticActionHelper helper) {
		builder.add(
				Fall.FALL.variate(
						AquaticPoundDrop.ID,
						data -> data.getInputs().DUCK.isHeld() && Fall.FALL.evaluator().shouldTransition(data),
						null,
						data -> data.setYVel(-0.6),
						(data, isSelf, seed) -> data.storeSound(data.playSound(MarioSFX.AQUATIC_GROUND_POUND_DROP, seed))
				),
				Fall.FALL.variate(Submerged.ID, null),
				UnderwaterWalk.EXIT_WATER
		);
	}
}