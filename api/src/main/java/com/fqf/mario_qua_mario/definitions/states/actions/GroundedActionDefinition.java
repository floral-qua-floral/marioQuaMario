package com.fqf.mario_qua_mario.definitions.states.actions;

import com.fqf.mario_qua_mario.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.CharaStat;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface GroundedActionDefinition extends IncompleteActionDefinition {
	void travelHook(IMarioTravelData data, GroundedActionHelper helper);

	@NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper);
	@NotNull List<TransitionDefinition> getInputTransitions(GroundedActionHelper helper);
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions(GroundedActionHelper helper);

	/**
	 * Contains a number of methods intended to help with the creation of Grounded Actions. Can be cast to any of the
	 * other ActionHelpers, if for whatever reason you need them.
	 */
	interface GroundedActionHelper {
		/**
		 * Accelerates Mario using a custom formula. This detaches Mario's ability to gain or lose speed from his
		 * ability to redirect the angle of existing speed. Unlike a regular entity, Mario's acceleration and
		 * decceleration is almost always linear, rather than being based on drag.
		 *
		 * @param forwardAccelStat The rate at which Mario's forward velocity will change. The absolute value is used.
		 * @param forwardSpeedStat The forward speed that Mario will try to reach. His acceleration won't overshoot
		 *                         this value; if {@code forwardAccelStat} is sufficient to accelerate him past it,
		 *                         he'll instead snap to its value. The value of this is scaled by the magnitude of
		 *                         Mario's forward input; when using a keyboard, the magnitude is either 0 or 1,
		 *                         but if the player is using a controller with an analog stick, it may be values in
		 *                         between.
		 * @param strafeAccelStat The rate at which Mario's sideways velocity will change. The absolute value is used.
		 * @param strafeSpeedStat The rightwards speed that Mario will try to accelerate towards. Works similarly to
		 *                        {@code forwardSpeedStat}. Negative values correspond to leftwards movement.
		 * @param forwardAngleContribution The forward component of Mario's intended angle of motion.
		 * @param strafeAngleContribution The rightward component of Mario's intended angle of motion.
		 * @param redirectStat How far, in degrees, Mario's current motion vector will rotate towards the vector made
		 *                     by {@code forwardAngleContribution} and {@code strafeAngleContribution}. This rotation
		 *                     occurs before acceleration; even with a {@code redirectStat} of 0 he'll still be able
		 *                     to change direction with his acceleration.
		 */
		void groundAccel(
				IMarioTravelData data,
				CharaStat forwardAccelStat, CharaStat forwardSpeedStat,
				CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
				double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
		);

		/**
		 * Reduces Mario's speed by a portion of his current speed. It is recommended to use this sparingly;
		 * normal Minecraft entities are subject to drag every tick, but Mario should only experience drag when
		 * he's in an explicitly sliding-related action (or underwater). Under other circumstances, he should use
		 * {@link #groundAccel}.
		 *
		 * @param drag The fractional portion of Mario's speed that should be lost each tick. For example, a drag of
		 *             0.1 will result in 10% of Mario's speed being lost per tick.
		 * @param dragMin The minimum amount of speed lost per tick. Used to ensure Mario comes to a prompt stop
		 *                instead of lingering on very low speed values.
		 * @param forwardAngleContribution The forward component of Mario's intended angle of motion.
		 * @param strafeAngleContribution The rightward component of Mario's intended angle of motion.
		 * @param redirection How far, in degrees, Mario's current motion vector will rotate towards the intended
		 *                    direction given by forwardAngleContribution and strafeAngleContribution.
		 */
		void applyDrag(
				IMarioTravelData data,
				CharaStat drag, CharaStat dragMin,
				double forwardAngleContribution, double strafeAngleContribution,
				CharaStat redirection
		);
	}
}
