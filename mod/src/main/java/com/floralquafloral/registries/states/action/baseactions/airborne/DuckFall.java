package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.registries.states.action.baseactions.grounded.DuckWaddle;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DuckFall extends Fall {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "duck_fall");
	}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.SLIP;
	}

	@Override public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:fall",
						DuckWaddle.UNDUCK.EVALUATOR
				)
		);
	}

	@Override public List<ActionTransitionDefinition> getPostTickTransitions() {
		return List.of();
	}

	@Override public List<ActionTransitionDefinition> getPostMoveTransitions() {
		return List.of(
				AerialTransitions.DUCKING_LANDING
		);
	}
}
