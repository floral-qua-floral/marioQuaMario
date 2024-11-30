package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioAuthoritativeData;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.definitions.actions.AirborneActionDefinition;
import com.floralquafloral.definitions.actions.CharaStat;
import com.floralquafloral.definitions.actions.StatCategory;
import com.floralquafloral.util.MarioSFX;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LongJump extends AirborneActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "long_jump");
	}
	@Override public @Nullable String getAnimationName() {
		return "long-jump";
	}
	@Override @Nullable public CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override @Nullable public BumpingRule getBumpingRule() {
		return new BumpingRule(4, 0, 3, 0.2);
	}

	public static final CharaStat GRAVITY = AerialStats.GRAVITY.variate(0.575);

	public static final CharaStat LONG_JUMP_VEL = new CharaStat(0.614, StatCategory.JUMP_VELOCITY);
//	public static final CharaStat LONG_JUMP_VEL = new CharaStat(0.858, StatCategory.JUMP_VELOCITY);
	public static final CharaStat LONG_JUMP_THRESHOLD = new CharaStat(0.285, StatCategory.THRESHOLD);

	public static CharaStat REDUCED_FORWARD_ACCEL = AerialStats.FORWARD_DRIFT_ACCEL.variate(0.5);
	public static CharaStat REDUCED_BACKWARD_ACCEL = AerialStats.BACKWARD_DRIFT_ACCEL.variate(0.5);
	public static CharaStat REDUCED_STRAFE_ACCEL = AerialStats.STRAFE_DRIFT_ACCEL.variate(0.5);

	public static CharaStat REDUCED_REDIRECTION = AerialStats.DRIFT_REDIRECTION.variate(0.66);

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.PROHIBIT;
	}
	@Override public SlidingStatus getActionSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return Identifier.of("qua_mario", "stomp");
	}

	@Override protected @NotNull CharaStat getGravity() {
		return GRAVITY;
	}
	@Override protected @Nullable CharaStat getJumpGravity() {
		return GRAVITY;
	}
	@Override protected @NotNull CharaStat getTerminalVelocity() {
		return AerialStats.TERMINAL_VELOCITY;
	}

	@Override public void airborneTravel(MarioTravelData data) {
		airborneAccel(data,
				REDUCED_FORWARD_ACCEL, AerialStats.FORWARD_DRIFT_SPEED,
				REDUCED_BACKWARD_ACCEL, AerialStats.BACKWARD_DRIFT_SPEED,
				REDUCED_STRAFE_ACCEL, AerialStats.STRAFE_DRIFT_SPEED,
				1.0, 1.0, REDUCED_REDIRECTION
		);
	}

	@Override public void clientTick(MarioClientSideData data, boolean isSelf) {

	}

	@Override public void serverTick(MarioAuthoritativeData data) {

	}

	@Override public List<ActionTransitionDefinition> getPreTravelTransitions() {
		return List.of();
	}

	@Override public List<ActionTransitionDefinition> getInputTransitions() {
		return List.of();
	}

	@Override public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of(
				AerialTransitions.ENTER_WATER,
				new ActionTransitionDefinition("qua_mario:bonk_air",
						data -> data.getTimers().bumpedWall != null,
						data -> {
							data.setYVel(Math.min(data.getYVel(), 0));
							if(data.getTimers().bumpedWall != null)
								data.getMario().setVelocity(data.getTimers().bumpedWall.getLeft().getAxis() == Direction.Axis.X
									? data.getTimers().bumpedWall.getRight().multiply(-1, 1, 1)
									: data.getTimers().bumpedWall.getRight().multiply(1, 1, -1));
						},
						(data, isSelf, seed) -> {
							data.voice(MarioClientSideData.VoiceLine.REVERT, seed);
							data.playSoundEvent(MarioSFX.BONK, seed);
						}
				),
				AerialTransitions.DUCKING_LANDING,
				AerialTransitions.DOUBLE_JUMPABLE_LANDING
		);
	}

	@Override public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of();
	}

	@Override public List<AttackInterceptionDefinition> getUnarmedAttackInterceptions() {
		return List.of();
	}
}
