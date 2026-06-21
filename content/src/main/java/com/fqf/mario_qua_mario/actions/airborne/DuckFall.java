package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.SneakingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.SprintingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.grounded.DuckWaddle;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DuckFall extends Fall implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("duck_fall");

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return DuckWaddle.makeAnimation(false, false);
	}

	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.ALLOW;
	}

	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	public static final ActionTransitionDetails DUCK_FALL = Fall.FALL.variate(DuckFall.ID, null);
	public static final ActionTransitionDetails DUCK_LANDING = Fall.LANDING.variate(DuckWaddle.ID, null);

	@Override public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {
		builder.add(
				DuckWaddle.UNDUCK.variate(Fall.ID, null)
		);
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {

	}

	@Override protected ActionTransitionDetails getLandingTransition() {
		return DUCK_LANDING;
	}
}
