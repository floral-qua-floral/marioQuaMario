package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.charaformact_api.definitions.TransitionInjectionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class GroundPoundLandHold extends GroundPoundLand implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("ground_pound_land_hold");

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return BonkGround.makeAnimation((data, animationTime) -> 0);
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {

	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(new ActionTransitionDetails(
				GroundPoundLand.ID,
				data -> !data.getInputs().DUCK.isHeld(),
				EvaluatorEnvironment.CLIENT_ONLY
		));
	}

	public static final TransitionInjectionDefinition INJECTION = new TransitionInjectionDefinition.Simple(
			TransitionInjectionDefinition.InjectionPlacement.BEFORE, GroundPoundLand.ID,
			ActionCategory.AIRBORNE,
			(nearbyTransition, castableHelper) -> new ActionTransitionDetails(
					GroundPoundLandHold.ID,
					data -> (data.isServer() || data.getInputs().DUCK.isHeld()) && nearbyTransition.evaluator().shouldTransition(data),
					EvaluatorEnvironment.CLIENT_CHECKED,
					nearbyTransition.travelExecutor(),
					nearbyTransition.clientsExecutor()
			)
	);
}
