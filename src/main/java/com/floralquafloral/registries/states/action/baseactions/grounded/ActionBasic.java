package com.floralquafloral.registries.states.action.baseactions.grounded;

import com.floralquafloral.CharaStat;
import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.Input;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.registries.states.action.GroundedActionDefinition;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.floralquafloral.CharaStat.*;

import java.util.List;

public class ActionBasic extends GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "basic");
	}
	@Override public @Nullable String getAnimationName() {
		return null;
	}

	@Override
	public void groundedSelfTick(MarioClientData data) {
		if(Input.getForwardInput() > 0) {
			boolean isRunning = data.getMario().isSprinting()
					&& data.getForwardVel() > WALK_SPEED.getAsThreshold(data);

			if(isRunning) {
				if(data.getForwardVel() > RUN_SPEED.getAsLimit(data)) {
					// Overrun
					groundAccel(data,
							OVERRUN_ACCEL.get(data), RUN_SPEED.get(data),
							STRAFE_ACCEL.get(data), STRAFE_SPEED.get(data),
							Input.getForwardInput(), Input.getStrafeInput() * 0.8,
							RUN_REDIRECTION.get(data)
					);
				}
				else {
					// Run Accel
					groundAccel(data,
							RUN_ACCEL.get(data), RUN_SPEED.get(data),
							STRAFE_ACCEL.get(data), STRAFE_SPEED.get(data),
							Input.getForwardInput(), Input.getStrafeInput(),
							RUN_REDIRECTION.get(data)
					);
				}
			}
			else {
				if(data.getForwardVel() > WALK_SPEED.getAsLimit(data)) {
					// Overwalk
					groundAccel(data,
							OVERWALK_ACCEL.get(data), WALK_SPEED.get(data),
							STRAFE_ACCEL.get(data), STRAFE_SPEED.get(data),
							Input.getForwardInput(), Input.getStrafeInput(),
							WALK_REDIRECTION.get(data)
					);
				}
				else if(data.getForwardVel() <= WALK_STANDSTILL_THRESHOLD.getAsThreshold(data)) {
					// Walk accel from low velocity
					groundAccel(data,
							WALK_STANDSTILL_ACCEL.get(data), WALK_SPEED.get(data),
							STRAFE_ACCEL.get(data), STRAFE_SPEED.get(data),
							Input.getForwardInput(), Input.getStrafeInput(),
							WALK_REDIRECTION.get(data)
					);
				}
				else {
					// Walk accel
					groundAccel(data,
							WALK_ACCEL.get(data), WALK_SPEED.get(data),
							STRAFE_ACCEL.get(data), STRAFE_SPEED.get(data),
							Input.getForwardInput(), Input.getStrafeInput(),
							WALK_REDIRECTION.get(data)
					);
				}
			}
		}
		else if(Input.getForwardInput() < 0) {
			if(data.getForwardVel() > 0) {
				// Under-backpedal
				groundAccel(data,
						UNDERBACKPEDAL_ACCEL.get(data), BACKPEDAL_SPEED.get(data),
						STRAFE_ACCEL.get(data), STRAFE_SPEED.get(data),
						Input.getForwardInput(), Input.getStrafeInput(),
						BACKPEDAL_REDIRECTION.get(data)
				);
			}
			else if(data.getForwardVel() < BACKPEDAL_SPEED.getAsLimit(data)) {
				// Over-backpedal
				groundAccel(data,
						OVERBACKPEDAL_ACCEL.get(data), BACKPEDAL_SPEED.get(data),
						STRAFE_ACCEL.get(data), STRAFE_SPEED.get(data),
						Input.getForwardInput(), Input.getStrafeInput(),
						BACKPEDAL_REDIRECTION.get(data)
				);
			}
			else {
				// Backpedal Accel
				groundAccel(data,
						BACKPEDAL_ACCEL.get(data), BACKPEDAL_SPEED.get(data),
						STRAFE_ACCEL.get(data), STRAFE_SPEED.get(data),
						Input.getForwardInput(), Input.getStrafeInput(),
						BACKPEDAL_REDIRECTION.get(data)
				);
			}
		}
		else {
			// Idle deaccel
			groundAccel(data,
					IDLE_DEACCEL.get(data), 0,
					STRAFE_ACCEL.get(data), STRAFE_SPEED.get(data),
					0.0, 0.0, 0.0
			);
		}
	}

	@Override public void otherClientsTick(MarioPlayerData data) {}

	@Override public void serverTick(MarioPlayerData data) {}

	@Override public SneakLegalityRule getSneakLegalityRule() {
		return SneakLegalityRule.ALLOW;
	}
	@Override public SlidingStatus getConstantSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}
	@Override public @Nullable Identifier getStompType() {
		return Identifier.of("qua_mario:stomp");
	}

	@Override
	public List<ActionTransitionDefinition> getPreTickTransitions() {
		return List.of(
//				GroundedTransitions.FALL,
				GroundedTransitions.DUCK_WADDLE,
				new ActionTransitionDefinition("qua_mario:p_run",
						(data) -> data.getForwardVel() >= RUN_SPEED.getAsThreshold(data),
						(data, isSelf, seed) -> {
							if(isSelf) data.setForwardVel(P_SPEED.get(data));
						},
						(data, seed) -> {

						}
				)
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
