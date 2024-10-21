package com.floralquafloral.registries.states.action.baseactions.grounded;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.Input;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.states.action.GroundedActionDefinition;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.floralquafloral.CharaStat.*;

public class PRun extends GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "p_run");
	}
	@Override public @Nullable String getAnimationName() {
		return "p-run";
	}

	@Override
	public void groundedSelfTick(MarioClientData data) {
		boolean sprinting = data.getMario().isSprinting();
		groundAccel(data,
				sprinting ? OVERRUN_ACCEL.get(data) : OVERWALK_ACCEL.get(data),
				sprinting ? P_SPEED.get(data) : WALK_SPEED.get(data),
				STRAFE_ACCEL.get(data), STRAFE_SPEED.get(data),
				Input.getForwardInput(), Input.getStrafeInput(),
				P_SPEED_REDIRECTION.get(data)
		);
	}

	@Override public void otherClientsTick(MarioPlayerData data) {}

	@Override public void serverTick(MarioPlayerData data) {}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.ALLOW;
	}
	@Override public SlidingStatus getConstantSlidingStatus() {
		return SlidingStatus.NOT_SLIDING_SMOOTH;
	}
	@Override public @Nullable Identifier getStompType() {
		return null;
	}

	@Override
	public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
//				GroundedTransitions.FALL,
				GroundedTransitions.DUCK_WADDLE,
				new ActionTransitionDefinition("qua_mario:basic",
						(data) -> data.getForwardVel() < RUN_SPEED.getAsThreshold(data))
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
