package com.floralquafloral.registries.action.baseactions.grounded;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.Input;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.action.GroundedActionDefinition;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.floralquafloral.CharaStat.*;

public class DuckWaddle extends GroundedActionDefinition {
	public static final ActionTransitionDefinition UNDUCK = new ActionTransitionDefinition(
			"qua_mario:basic",
			(data) -> !Input.DUCK.isHeld()
	);

	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "duck_waddle");
	}
	@Override public @Nullable String getAnimationName() {
		return "duck_waddle";
	}

	@Override
	public void groundedSelfTick(MarioClientData data) {
		boolean waddlingForward = data.getForwardVel() > 0;
		groundAccel(data,
				waddlingForward ? WADDLE_ACCEL.get(data) : WADDLE_BACKPEDAL_ACCEL.get(data),
				waddlingForward ? WADDLE_SPEED.get(data) : WADDLE_BACKPEDAL_SPEED.get(data),
				WADDLE_STRAFE_ACCEL.get(data), WADDLE_STRAFE_SPEED.get(data),
				Input.getForwardInput(), Input.getStrafeInput(), WADDLE_REDIRECTION.get(data)
		);
	}

	@Override public void otherClientsTick(MarioPlayerData data) {}

	@Override public void serverTick(MarioPlayerData data) {}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.ALLOW;
	}
	@Override public SlidingStatus getConstantSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override
	public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
				UNDUCK
		);
	}

	@Override
	public List<ActionTransitionDefinition> getPostTickTransitions() {
		return List.of();
	}

	@Override
	public List<ActionTransitionDefinition> getPostMoveTransitions() {
		return List.of();
	}

	@Override
	public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of();
	}
}
