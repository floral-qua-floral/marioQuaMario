package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.states.action.AirborneActionDefinition;
import com.floralquafloral.registries.states.action.baseactions.grounded.DuckWaddle;
import com.floralquafloral.stats.CharaStat;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DuckFall extends Fall {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "duck_fall");
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
				new ActionTransitionDefinition("qua_mario:fall",
						DuckWaddle.UNDUCK.EVALUATOR
				)
		);
	}
}
