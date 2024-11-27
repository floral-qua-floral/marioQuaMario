package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.registries.states.action.baseactions.grounded.DuckWaddle;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DuckJump extends Jump {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "duck_jump");
	}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.ALLOW;
	}

	@Override public List<ActionTransitionDefinition> getPreTravelTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:jump",
						DuckWaddle.UNDUCK.EVALUATOR
				)
		);
	}

	@Override public List<ActionTransitionDefinition> getInputTransitions() {
		return List.of(
				AerialTransitions.makeJumpCapTransition(this, 0.14)
		);
	}

	@Override
	public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of(
				AerialTransitions.ENTER_WATER,
				AerialTransitions.DUCKING_LANDING
		);
	}

	@Override public List<AttackInterceptionDefinition> getUnarmedAttackInterceptions() {
		return List.of();
	}
}
