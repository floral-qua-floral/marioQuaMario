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
import org.joml.Vector2d;

import static com.floralquafloral.stats.StatCategory.*;

import java.util.List;

public class ActionBasic extends GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "basic");
	}
	@Override public @Nullable String getAnimationName() {
		return null;
	}

	public static final CharaStat WALK_ACCEL = new CharaStat(0.045, WALKING, FORWARD, ACCELERATION);
	public static final CharaStat WALK_STANDSTILL_ACCEL = new CharaStat(0.125, WALKING, FORWARD, ACCELERATION, FRICTION);
	public static final CharaStat WALK_STANDSTILL_THRESHOLD = new CharaStat(0.12, WALKING, THRESHOLD);
	public static final CharaStat WALK_SPEED = new CharaStat(0.275, WALKING, SPEED, FORWARD);
	public static final CharaStat WALK_REDIRECTION = new CharaStat(0.0, WALKING, FORWARD, REDIRECTION);

	public static final CharaStat OVERWALK_ACCEL = new CharaStat(0.028, WALKING, FORWARD, OVERSPEED_CORRECTION);

	public static final CharaStat IDLE_DEACCEL = new CharaStat(0.075, WALKING, FRICTION);

	public static final CharaStat BACKPEDAL_ACCEL = new CharaStat(0.055, WALKING, BACKWARD, ACCELERATION);
	public static final CharaStat BACKPEDAL_SPEED = new CharaStat(0.225, WALKING, BACKWARD, SPEED);
	public static final CharaStat BACKPEDAL_REDIRECTION = new CharaStat(0.0, WALKING, BACKWARD, REDIRECTION);
	public static final CharaStat OVERBACKPEDAL_ACCEL = new CharaStat(0.04, WALKING, BACKWARD, OVERSPEED_CORRECTION);
	public static final CharaStat UNDERBACKPEDAL_ACCEL = new CharaStat(0.055, WALKING, BACKWARD, ACCELERATION, FRICTION);

	public static final CharaStat RUN_ACCEL = new CharaStat(0.0102, RUNNING, FORWARD, ACCELERATION);
	public static final CharaStat RUN_SPEED = new CharaStat(0.575, RUNNING, FORWARD, SPEED);
	public static final CharaStat RUN_REDIRECTION = new CharaStat(2.76, RUNNING, FORWARD, REDIRECTION);
	public static final CharaStat OVERRUN_ACCEL = new CharaStat(0.0175, RUNNING, FORWARD, OVERSPEED_CORRECTION);

	public static final CharaStat STRAFE_ACCEL = new CharaStat(0.065, WALKING, STRAFE, ACCELERATION);
	public static final CharaStat STRAFE_SPEED = new CharaStat(0.275, WALKING, STRAFE, SPEED);

	@Override
	public void groundedTravel(MarioClientData data) {
		if(Input.getForwardInput() > 0) {
			double walkThreshold = WALK_SPEED.getAsThreshold(data);
			boolean isRunning = data.getMario().isSprinting()
					&& data.getForwardVel() > WALK_STANDSTILL_THRESHOLD.get(data)
					&& Vector2d.lengthSquared(data.getForwardVel(), data.getStrafeVel()) > walkThreshold * walkThreshold;

			if(isRunning) {
				if(data.getForwardVel() > RUN_SPEED.getAsLimit(data)) {
					// Overrun
					groundAccel(data,
							OVERRUN_ACCEL, RUN_SPEED,
							STRAFE_ACCEL, STRAFE_SPEED,
							Input.getForwardInput(), Input.getStrafeInput() * 0.8,
							RUN_REDIRECTION
					);
				}
				else {
					// Run Accel
					groundAccel(data,
							RUN_ACCEL, RUN_SPEED,
							STRAFE_ACCEL, STRAFE_SPEED,
							Input.getForwardInput(), Input.getStrafeInput(),
							RUN_REDIRECTION
					);
				}
			}
			else {
				if(data.getForwardVel() > WALK_SPEED.getAsLimit(data)) {
					// Overwalk
					groundAccel(data,
							OVERWALK_ACCEL, WALK_SPEED,
							STRAFE_ACCEL, STRAFE_SPEED,
							Input.getForwardInput(), Input.getStrafeInput(),
							WALK_REDIRECTION
					);
				}
				else if(data.getForwardVel() <= WALK_STANDSTILL_THRESHOLD.getAsThreshold(data)) {
					// Walk accel from low velocity
					groundAccel(data,
							WALK_STANDSTILL_ACCEL, WALK_SPEED,
							STRAFE_ACCEL, STRAFE_SPEED,
							Input.getForwardInput(), Input.getStrafeInput(),
							WALK_REDIRECTION
					);
				}
				else {
					// Walk accel
					groundAccel(data,
							WALK_ACCEL, WALK_SPEED,
							STRAFE_ACCEL, STRAFE_SPEED,
							Input.getForwardInput(), Input.getStrafeInput(),
							WALK_REDIRECTION
					);
				}
			}
		}
		else if(Input.getForwardInput() < 0) {
			if(data.getForwardVel() > 0) {
				// Under-backpedal
				groundAccel(data,
						UNDERBACKPEDAL_ACCEL, BACKPEDAL_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						Input.getForwardInput(), Input.getStrafeInput(),
						BACKPEDAL_REDIRECTION
				);
			}
			else if(data.getForwardVel() < BACKPEDAL_SPEED.getAsLimit(data)) {
				// Over-backpedal
				groundAccel(data,
						OVERBACKPEDAL_ACCEL, BACKPEDAL_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						Input.getForwardInput(), Input.getStrafeInput(),
						BACKPEDAL_REDIRECTION
				);
			}
			else {
				// Backpedal Accel
				groundAccel(data,
						BACKPEDAL_ACCEL, BACKPEDAL_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						Input.getForwardInput(), Input.getStrafeInput(),
						BACKPEDAL_REDIRECTION
				);
			}
		}
		else {
			// Idle deaccel
			groundAccel(data,
					IDLE_DEACCEL, ZERO,
					STRAFE_ACCEL, STRAFE_SPEED,
					0.0, 0.0, ZERO
			);
		}
	}

	@Override public void clientTick(MarioPlayerData data, boolean isSelf) {}

	@Override public void serverTick(MarioPlayerData data) {}

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
				GroundedTransitions.FALL,
				GroundedTransitions.DUCK_WADDLE,
				new ActionTransitionDefinition("qua_mario:p_run",
						(data) -> data.getForwardVel() >= RUN_SPEED.getAsThreshold(data),
						(data, isSelf, seed) -> {
							if(isSelf) data.setForwardVel(Math.max(PRun.P_SPEED.get(data), data.getForwardVel()));
						},
						(data, seed) -> {

						}
				),
				Skid.SKID_TRANSITION
		);
	}

	@Override
	public List<ActionTransitionDefinition> getPostTickTransitions() {
		return List.of(
				GroundedTransitions.JUMP
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
