package com.fqf.mario_qua_mario_content.actions.grounded;

import com.fqf.mario_qua_mario_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.ActionCategory;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class GroundPoundLandHold extends GroundPoundLand implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("ground_pound_land_hold");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return BonkGroundBackward.makeBonkStandupAnimation(helper, (data, ticksPassed) -> 0);
	}

	@Override
	public @NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper) {
		return List.of();
	}

	@Override
	public @NotNull List<TransitionDefinition> getInputTransitions(GroundedActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						GroundPoundLand.ID,
						data -> !data.getInputs().DUCK.isHeld(),
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
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
