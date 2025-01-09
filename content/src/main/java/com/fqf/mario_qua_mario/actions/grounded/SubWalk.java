package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.Jump;
import com.fqf.mario_qua_mario.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.CharaStat;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario.util.StatCategory.*;

public class SubWalk implements GroundedActionDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("sub_walk");
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return null;
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.ALLOW;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable BumpType getBumpType() {
		return null;
	}
	@Override public @Nullable Identifier getStompTypeID() {
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

	public static final CharaStat STRAFE_ACCEL = new CharaStat(0.065, WALKING, STRAFE, ACCELERATION);
	public static final CharaStat STRAFE_SPEED = new CharaStat(0.275, WALKING, STRAFE, SPEED);

	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, GroundedActionHelper helper) {
		if(data.getInputs().getForwardInput() > 0) {
			if(data.getForwardVel() > WALK_SPEED.getAsLimit(data)) {
				// Overwalk
				helper.groundAccel(data,
						OVERWALK_ACCEL, WALK_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
						WALK_REDIRECTION
				);
			}
			else if(data.getForwardVel() <= WALK_STANDSTILL_THRESHOLD.getAsThreshold(data)) {
				// Walk accel from low velocity
				helper.groundAccel(data,
						WALK_STANDSTILL_ACCEL, WALK_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
						WALK_REDIRECTION
				);
			}
			else {
				// Walk accel
				helper.groundAccel(data,
						WALK_ACCEL, WALK_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
						WALK_REDIRECTION
				);
			}
		}
		else if(data.getInputs().getForwardInput() < 0) {
			if(data.getForwardVel() > 0) {
				// Under-backpedal
				helper.groundAccel(data,
						UNDERBACKPEDAL_ACCEL, BACKPEDAL_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
						BACKPEDAL_REDIRECTION
				);
			}
			else if(data.getForwardVel() < BACKPEDAL_SPEED.getAsLimit(data)) {
				// Over-backpedal
				helper.groundAccel(data,
						OVERBACKPEDAL_ACCEL, BACKPEDAL_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
						BACKPEDAL_REDIRECTION
				);
			}
			else {
				// Backpedal Accel
				helper.groundAccel(data,
						BACKPEDAL_ACCEL, BACKPEDAL_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
						BACKPEDAL_REDIRECTION
				);
			}
		}
		else {
			// Idle deaccel
			helper.groundAccel(data,
					IDLE_DEACCEL, CharaStat.ZERO,
					STRAFE_ACCEL, STRAFE_SPEED,
					0.0, 0.0, CharaStat.ZERO
			);
		}
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper) {
		return List.of(
			new TransitionDefinition(
					MarioQuaMarioContent.makeID("walk_run"),
					WalkRun::meetsWalkRunRequirement,
					EvaluatorEnvironment.CLIENT_ONLY
			)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(GroundedActionHelper helper) {
		return List.of(
				Jump.makeJumpTransition(helper)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(GroundedActionHelper helper) {
		return List.of(
				Fall.FALL
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions() {
		return List.of();
	}
}
