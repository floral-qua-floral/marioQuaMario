package com.floralquafloral.registries.states.action.baseactions.grounded;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import com.floralquafloral.registries.states.action.GroundedActionDefinition;
import com.floralquafloral.registries.states.action.baseactions.airborne.Jump;
import com.floralquafloral.stats.CharaStat;
import com.floralquafloral.util.MarioSFX;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.floralquafloral.stats.StatCategory.*;

public class DuckWaddle extends GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "duck_waddle");
	}
	@Override public @Nullable String getAnimationName() {
		return "duck_waddle";
	}

	public static final ActionTransitionDefinition UNDUCK = new ActionTransitionDefinition(
			"qua_mario:basic",
			(data) -> !data.getInputs().DUCK.isHeld(),
			null,
			(data, isSelf, seed) -> data.playSoundEvent(MarioSFX.UNDUCK, seed)
	);
	public static final ActionTransitionDefinition DUCK_FALL = new ActionTransitionDefinition(
			"qua_mario:duck_fall",
			GroundedTransitions.FALL.EVALUATOR
	);
	public static final ActionTransitionDefinition DUCK_JUMP = new ActionTransitionDefinition(
			"qua_mario:duck_jump",
			GroundedTransitions.JUMP.EVALUATOR,
			data -> GroundedTransitions.performJump(data, Jump.JUMP_VEL, null),
			(data, isSelf, seed) -> {
				data.voice(MarioClientSideData.VoiceLine.DUCK_JUMP, seed);
				data.playJumpSound(seed);
			}
	);
	public static final ActionTransitionDefinition BACKFLIP = new ActionTransitionDefinition(
			"qua_mario:backflip",
			data -> data.getInputs().JUMP.isPressed() && data.getForwardVel() < -0.05 && data.getInputs().getForwardInput() < 0.5
	);

	public static final CharaStat WADDLE_ACCEL = new CharaStat(0.06, DUCKING, FORWARD, ACCELERATION);
	public static final CharaStat WADDLE_SPEED = new CharaStat(0.08, DUCKING, FORWARD, SPEED);

	public static final CharaStat WADDLE_BACKPEDAL_ACCEL = new CharaStat(0.0725, DUCKING, BACKWARD, ACCELERATION);
	public static final CharaStat WADDLE_BACKPEDAL_SPEED = new CharaStat(0.06, DUCKING, BACKWARD, SPEED);

	public static final CharaStat WADDLE_STRAFE_ACCEL = new CharaStat(0.06, DUCKING, STRAFE, ACCELERATION);
	public static final CharaStat WADDLE_STRAFE_SPEED = new CharaStat(0.06, DUCKING, STRAFE, SPEED);

	public static final CharaStat WADDLE_REDIRECTION = new CharaStat(0.0, DUCKING, REDIRECTION);

	@Override
	public void groundedTravel(MarioTravelData data) {
		boolean waddlingForward = data.getForwardVel() > 0;
		groundAccel(data,
				waddlingForward ? WADDLE_ACCEL : WADDLE_BACKPEDAL_ACCEL,
				waddlingForward ? WADDLE_SPEED : WADDLE_BACKPEDAL_SPEED,
				WADDLE_STRAFE_ACCEL, WADDLE_STRAFE_SPEED,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(), WADDLE_REDIRECTION
		);
	}

	@Override public void clientTick(MarioClientSideData data, boolean isSelf) {}

	@Override public void serverTick(MarioServerData data) {}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.ALLOW;
	}
	@Override public SlidingStatus getConstantSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return null;
	}

	@Override
	public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
				DUCK_FALL
		);
	}

	@Override
	public List<ActionTransitionDefinition> getPostTickTransitions() {
		return List.of(
				UNDUCK,
				DUCK_JUMP
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
