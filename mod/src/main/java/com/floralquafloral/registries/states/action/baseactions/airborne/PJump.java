package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.registries.states.action.baseactions.grounded.ActionBasic;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PJump extends Jump {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "p_jump");
	}
	@Override public @Nullable String getAnimationName() {
		return "p-jump";
	}

	@Override
	public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of(
				CommonTransitions.ENTER_WATER,
				new ActionTransitionDefinition("qua_mario:p_run",
						(data) -> data.getMario().isOnGround() && data.getForwardVel() >= ActionBasic.RUN_SPEED.getAsThreshold(data),
						AerialTransitions.DOUBLE_JUMPABLE_LANDING.EXECUTOR_TRAVELLERS,
						AerialTransitions.DOUBLE_JUMPABLE_LANDING.EXECUTOR_CLIENTS
				),
				AerialTransitions.DOUBLE_JUMPABLE_LANDING
		);
	}
}
