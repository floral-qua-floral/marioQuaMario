package com.floralquafloral.definitions.actions;

import com.floralquafloral.util.MarioSFX;
import com.floralquafloral.mariodata.MarioTravelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.floralquafloral.definitions.actions.StatCategory.*;

public abstract class AirborneActionDefinition implements ActionDefinition {
	public abstract static class AerialTransitions {
		public static ActionTransitionDefinition makeJumpCapTransition(ActionDefinition forAction, double capThreshold) {
			CharaStat cap = new CharaStat(capThreshold, JUMP_CAP);
			return new ActionTransitionDefinition(
					forAction.getID().toString(),
					data -> !data.getTimers().jumpCapped && (!data.getInputs().JUMP.isHeld() || data.getYVel() < cap.get(data)),
					data -> {
						data.setYVel(Math.min(cap.get(data), data.getYVel()));
						data.getTimers().jumpCapped = true;
					},
					(data, isSelf, seed) -> data.fadeJumpSound()
			);
		}

		public static final ActionTransitionDefinition BASIC_LANDING = new ActionTransitionDefinition(
				"qua_mario:basic",
				(data) -> data.getMario().isOnGround()
		);
		public static final ActionTransitionDefinition DOUBLE_JUMPABLE_LANDING = new ActionTransitionDefinition(
				"qua_mario:basic",
				BASIC_LANDING.EVALUATOR,
				data -> data.getTimers().jumpLandingTime = 5,
				null
		);
		public static final ActionTransitionDefinition TRIPLE_JUMPABLE_LANDING = new ActionTransitionDefinition(
				"qua_mario:basic",
				BASIC_LANDING.EVALUATOR,
				data -> data.getTimers().doubleJumpLandingTime = 5,
				null
		);

		public static final ActionTransitionDefinition DUCKING_LANDING = new ActionTransitionDefinition(
				"qua_mario:duck_waddle",
				data -> data.getInputs().DUCK.isHeld() && AerialTransitions.BASIC_LANDING.EVALUATOR.shouldTransition(data),
				data -> data.setForwardStrafeVel(0.0, 0.0),
				null
		);

		public static final ActionTransitionDefinition GROUND_POUND = new ActionTransitionDefinition(
				"qua_mario:ground_pound_windup",
				data -> data.getInputs().DUCK.isPressed(),
				data -> data.getMario().fallDistance *= 0.4F,
				(data, isSelf, seed) -> data.playSoundEvent(MarioSFX.GROUND_POUND_PRE, seed)
		);

		public static final ActionTransitionDefinition ENTER_WATER = new ActionTransitionDefinition(
				"qua_mario:submerged",
				GroundedActionDefinition.GroundedTransitions.ENTER_WATER.EVALUATOR,
				GroundedActionDefinition.GroundedTransitions.ENTER_WATER.EXECUTOR_TRAVELLERS,
				GroundedActionDefinition.GroundedTransitions.ENTER_WATER.EXECUTOR_CLIENTS
		);
	}

	public abstract static class AerialStats {
		public static final CharaStat GRAVITY = new CharaStat(-0.115, NORMAL_GRAVITY);
		public static final CharaStat TERMINAL_VELOCITY = new CharaStat(-3.25, StatCategory.TERMINAL_VELOCITY);

		public static final CharaStat JUMP_GRAVITY = new CharaStat(-0.095, JUMPING_GRAVITY);

		public static final CharaStat FORWARD_DRIFT_ACCEL = new CharaStat(0.045, DRIFTING, FORWARD, ACCELERATION);
		public static final CharaStat FORWARD_DRIFT_SPEED = new CharaStat(0.275, DRIFTING, FORWARD, SPEED);
		public static final CharaStat FORWARD_DRIFT_SPRINT_SPEED = new CharaStat(0.275, DRIFTING, FORWARD, SPEED);

		public static final CharaStat BACKWARD_DRIFT_ACCEL = new CharaStat(0.055, DRIFTING, BACKWARD, ACCELERATION);
		public static final CharaStat BACKWARD_DRIFT_SPEED = new CharaStat(0.2, DRIFTING, BACKWARD, SPEED);

		public static final CharaStat STRAFE_DRIFT_ACCEL = new CharaStat(0.065, DRIFTING, STRAFE, ACCELERATION);
		public static final CharaStat STRAFE_DRIFT_SPEED = new CharaStat(0.25, DRIFTING, STRAFE, SPEED);

		public static final CharaStat DRIFT_REDIRECTION = new CharaStat(6.0, DRIFTING, REDIRECTION);
	}

	private final @NotNull CharaStat ACTION_GRAVITY = getGravity();
	private final @Nullable CharaStat ACTION_JUMP_GRAVITY = getJumpGravity();
	private final @NotNull CharaStat ACTION_TERMINAL_VELOCITY = getTerminalVelocity();

	protected abstract @NotNull CharaStat getGravity();
	protected abstract @Nullable CharaStat getJumpGravity();
	protected abstract @NotNull CharaStat getTerminalVelocity();

	@Override public final void travelHook(MarioTravelData data) {
		double yVel = data.getYVel();
		double terminalVelocity = ACTION_TERMINAL_VELOCITY.get(data);

		if(yVel > terminalVelocity) {
			CharaStat useGravity = (ACTION_JUMP_GRAVITY == null || data.getTimers().jumpCapped) ? ACTION_GRAVITY : ACTION_JUMP_GRAVITY;
			yVel += useGravity.get(data);

			data.setYVel(Math.max(terminalVelocity, yVel));
		}

		airborneTravel(data);
	}

	public abstract void airborneTravel(MarioTravelData data);

	public static void airborneAccel(
			MarioTravelData data,
			CharaStat accelStat, CharaStat speedStat,
			CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
	) {
		double forwardInput = data.getInputs().getForwardInput();
		double strafeInput = data.getInputs().getStrafeInput();
		double forwardVel = data.getForwardVel();
		double strafeVel = data.getStrafeVel();

		double accelValue, strafeAccelValue;

		if(forwardInput != 0 && (Math.signum(forwardVel) != Math.signum(forwardInput) || Math.abs(forwardVel) < Math.abs(speedStat.get(data))))
			accelValue = accelStat.get(data) * forwardInput;
		else accelValue = 0;

		if(strafeInput != 0 && (Math.signum(strafeVel) != Math.signum(strafeInput) || Math.abs(strafeVel) < Math.abs(strafeSpeedStat.get(data))))
			strafeAccelValue = strafeAccelStat.get(data) * strafeInput;
		else strafeAccelValue = 0;

		data.approachAngleAndAccel(
				accelValue, speedStat.get(data) * Math.signum(forwardInput),
				strafeAccelValue, strafeSpeedStat.get(data) * Math.signum(strafeInput),
				forwardAngleContribution * forwardInput,
				strafeAngleContribution * strafeInput,
				redirectStat.get(data)
		);
	}
	public static void airborneAccel(
			MarioTravelData data,
			CharaStat forwardAccelStat, CharaStat forwardSpeedStat,
			CharaStat backwardAccelStat, CharaStat backwardSpeedStat,
			CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
	) {
		// Unlike when on the ground, when Mario is in midair, neutral inputs don't cause him to accelerate towards 0.
		// He only accelerates when actively making an input, and will always try to accelerate in that direction, never backwards.
		boolean forwards = data.getInputs().getForwardInput() >= 0;
		airborneAccel(data,
				forwards ? forwardAccelStat : backwardAccelStat,
				forwards ? forwardSpeedStat : backwardSpeedStat,
				strafeAccelStat, strafeSpeedStat,
				forwardAngleContribution, strafeAngleContribution, redirectStat
		);
	}
	public static void airborneAccel(MarioTravelData data) {
		airborneAccel(data,
				AerialStats.FORWARD_DRIFT_ACCEL, AerialStats.FORWARD_DRIFT_SPEED,
				AerialStats.BACKWARD_DRIFT_ACCEL, AerialStats.BACKWARD_DRIFT_SPEED,
				AerialStats.STRAFE_DRIFT_ACCEL, AerialStats.STRAFE_DRIFT_SPEED,
				1, 1, AerialStats.DRIFT_REDIRECTION
		);
	}
}
