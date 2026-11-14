package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.grounded.DuckWaddle;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class DuckFall extends Fall implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("duck_fall");

	@Override public float defineHitboxHeight() {
		return DuckWaddle.DUCK_HEIGHT;
	}

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return DuckWaddle.makeAnimation(false, false);
	}

	@Override public void defineActionFlags(ImmutableSet.Builder<ActionFlag> builder) {
		super.defineActionFlags(builder);
		builder.addAll(ActionFlag.SNEAKING);
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
