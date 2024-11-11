package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.stats.CharaStat;
import com.floralquafloral.stats.StatCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Stomp extends Jump {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "stomp");
	}

	@Override public List<ActionTransitionDefinition> getPostTickTransitions() {
		return List.of(
				AerialTransitions.makeJumpCapTransition(this, 0.65)
		);
	}
}
