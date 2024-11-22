package com.floralquafloral.registries.states.action.baseactions.aquatic;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.util.MarioSFX;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Swim extends Submerged {
	public static final ActionTransitionDefinition SWIM = new ActionTransitionDefinition(
			"qua_mario:swim",
			data -> data.getTimers().actionTimer > 3 && data.getInputs().JUMP.isPressed(),
			data -> {
				data.setYVel(Math.min(0.45, data.getYVel() + 0.4));
				data.getTimers().actionTimer = 0;
			},
			(data, isSelf, seed) -> data.playSoundEvent(MarioSFX.SWIM, seed)
	);

	@Override
	public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "swim");
	}

	@Override
	public @Nullable String getAnimationName() {
		return "swim";
	}
}
