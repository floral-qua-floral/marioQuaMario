package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class GroundPoundLandHold extends GroundPoundLand implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("ground_pound_land_hold");
	@Override public @NotNull Identifier defineID() {
	    return ID;
	}

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return BonkGroundBackward.makeAnimation((data, animationTime) -> 0);
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<TransitionDefinition> builder, GroundedActionHelper helper) {

	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<TransitionDefinition> builder, GroundedActionHelper helper) {
		builder.add(new TransitionDefinition(
				GroundPoundLand.ID,
				data -> !data.getInputs().DUCK.isHeld(),
				EvaluatorEnvironment.CLIENT_ONLY
		));
	}

	@Override
	public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of(
				new TransitionInjectionDefinition(
						TransitionInjectionDefinition.InjectionPlacement.BEFORE,
						GroundPoundLand.ID,
						ActionCategory.AIRBORNE,
						(nearbyTransition, castableHelper) -> new TransitionDefinition(
								GroundPoundLandHold.ID,
								data -> (data.isServer() || data.getInputs().DUCK.isHeld()) && nearbyTransition.evaluator().shouldTransition(data),
								EvaluatorEnvironment.CLIENT_CHECKED,
								nearbyTransition.travelExecutor(),
								nearbyTransition.clientsExecutor()
						)
				)
		);
	}
}
