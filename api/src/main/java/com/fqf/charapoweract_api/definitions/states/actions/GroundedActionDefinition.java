package com.fqf.charapoweract_api.definitions.states.actions;

import com.fqf.charapoweract_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charapoweract_api.cpadata.ICPAReadableMotionData;
import com.fqf.charapoweract_api.cpadata.ICPATravelData;
import com.fqf.charapoweract_api.util.CharaStat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface GroundedActionDefinition extends IncompleteActionDefinition {
	void travelHook(ICPATravelData data, GroundedActionHelper helper);

	@NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper);
	@NotNull List<TransitionDefinition> getInputTransitions(GroundedActionHelper helper);
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions(GroundedActionHelper helper);

	/**
	 * Contains a number of methods intended to help with the creation of Grounded Actions. Can be cast to any of the
	 * other ActionHelpers, if for whatever reason you need them.
	 */
	interface GroundedActionHelper {
		/**
		 * Accelerates the player using a custom formula. This detaches her ability to gain or lose speed from her
		 * ability to redirect the angle of existing speed. Unlike a regular entity, a character's acceleration and
		 * decceleration is almost always linear, rather than being based on drag.
		 *
		 * @param forwardAccelStat The rate at which the player's forward velocity will change. The absolute value is used.
		 * @param forwardSpeedStat The forward speed that the player will try to reach. Her acceleration won't overshoot
		 *                         this value; if {@code forwardAccelStat} is sufficient to accelerate her past it,
		 *                         she'll instead snap to its value. The value of this is scaled by the magnitude of
		 *                         the current forward input; when using a keyboard, the magnitude is either 0 or 1,
		 *                         but if the player is using a controller with an analog stick, it may be values in
		 *                         between.
		 * @param strafeAccelStat The rate at which the player's sideways velocity will change. The absolute value is used.
		 * @param strafeSpeedStat The rightwards speed that the player will try to accelerate towards. Works similarly
		 *                        to {@code forwardSpeedStat}. Negative values correspond to leftwards movement.
		 * @param forwardAngleContribution The forward component of the player's intended angle of motion.
		 * @param strafeAngleContribution The rightward component of the player's intended angle of motion.
		 * @param redirectStat How far, in degrees, the player's current motion vector will rotate towards the vector made
		 *                     by {@code forwardAngleContribution} and {@code strafeAngleContribution}. This rotation
		 *                     occurs before acceleration; even with a {@code redirectStat} of 0 she'll still be able
		 *                     to change direction with her acceleration.
		 */
		void groundAccel(
				ICPATravelData data,
				CharaStat forwardAccelStat, CharaStat forwardSpeedStat,
				CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
				double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
		);

		/**
		 * Reduces the player's speed by a portion of her current speed. Mario seldom uses this, but typical
		 * Minecraft entities do something like this every tick.
		 *
		 * @param drag The fractional portion of the player's speed that should be lost each tick. For example, a drag
		 *             of 0.1 will result in 10% of the player's current speed being lost per tick.
		 * @param dragMin The minimum amount of speed lost per tick. Used to ensure the player comes to a prompt stop
		 *                instead of lingering on very low speed values.
		 * @param forwardAngleContribution The forward component of the player's intended angle of motion.
		 * @param strafeAngleContribution The rightward component of the player's intended angle of motion.
		 * @param redirection How far, in degrees, the player's current motion vector will rotate towards the intended
		 *                    direction given by forwardAngleContribution and strafeAngleContribution.
		 */
		void applyDrag(
				ICPATravelData data,
				CharaStat drag, CharaStat dragMin,
				double forwardAngleContribution, double strafeAngleContribution,
				CharaStat redirection
		);

		double getSlipFactor(ICPAReadableMotionData data);

		void performJump(
				ICPATravelData data,
				CharaStat jumpVel,
				@Nullable CharaStat speedAddend
		);
	}
}
