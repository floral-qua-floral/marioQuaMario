package com.floralquafloral.registries.states.action.baseactions.grounded;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.Input;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.states.action.GroundedActionDefinition;
import com.floralquafloral.stats.CharaStat;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.floralquafloral.stats.StatCategory.*;

public class PRun extends GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "p_run");
	}
	@Override public @Nullable String getAnimationName() {
		return "p-run";
	}

	public static final CharaStat P_ACCEL = new CharaStat(0.13, P_RUNNING, FORWARD, ACCELERATION);
	public static final CharaStat P_SPEED = new CharaStat(0.665, P_RUNNING, FORWARD, SPEED);
	public static final CharaStat P_REDIRECTION = new CharaStat(6.0, P_RUNNING, FORWARD, REDIRECTION);

	@Override
	public void groundedSelfTick(MarioClientData data) {
		boolean sprinting = data.getMario().isSprinting();
		groundAccel(data,
				sprinting ? ActionBasic.OVERRUN_ACCEL : ActionBasic.OVERWALK_ACCEL,
				sprinting ? P_SPEED : ActionBasic.WALK_SPEED,
				ActionBasic.STRAFE_ACCEL, ActionBasic.STRAFE_SPEED,
				Input.getForwardInput(), Input.getStrafeInput(),
				P_REDIRECTION
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
				GroundedTransitions.FALL,
				GroundedTransitions.DUCK_WADDLE,
				new ActionTransitionDefinition("qua_mario:basic",
						(data) -> data.getForwardVel() < ActionBasic.RUN_SPEED.getAsThreshold(data))
		);
	}

	@Override
	public List<ActionTransitionDefinition> getPostTickTransitions() {
		return List.of(
				new ActionTransitionDefinition("qua_mario:p_jump",
						GroundedTransitions.JUMP.EVALUATOR,
						GroundedTransitions.JUMP.EXECUTOR_CLIENT,
						GroundedTransitions.JUMP.EXECUTOR_SERVER
				)
		);
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
