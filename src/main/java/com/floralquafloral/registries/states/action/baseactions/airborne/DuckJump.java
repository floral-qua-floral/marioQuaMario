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

	@Override public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:duck_waddle",
						AerialTransitions.BASIC_LANDING.EVALUATOR
				)
		);
	}

	@Override public List<ActionTransitionDefinition> getPostTickTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:jump",
						DuckWaddle.UNDUCK.EVALUATOR
				)
		);
	}
}