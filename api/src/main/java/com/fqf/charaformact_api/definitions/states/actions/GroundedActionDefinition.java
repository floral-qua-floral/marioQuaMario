package com.fqf.charaformact_api.definitions.states.actions;

import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.util.CfaStat;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

public interface GroundedActionDefinition extends IncompleteActionDefinition {
	void travelHook(CfaTravelData data, GroundedActionHelper helper);

	default void accumulateBasicTransitions(ImmutableList.Builder<TransitionDefinition> builder, GroundedActionHelper helper) {

	}
	default void accumulateInputTransitions(ImmutableList.Builder<TransitionDefinition> builder, GroundedActionHelper helper) {

	}
	default void accumulateCollisionTransitions(ImmutableList.Builder<TransitionDefinition> builder, GroundedActionHelper helper) {

	}

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
				CfaTravelData data,
				CfaStat forwardAccelStat, CfaStat forwardSpeedStat,
				CfaStat strafeAccelStat, CfaStat strafeSpeedStat,
				double forwardAngleContribution, double strafeAngleContribution, CfaStat redirectStat
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
				CfaTravelData data,
				CfaStat drag, CfaStat dragMin,
				double forwardAngleContribution, double strafeAngleContribution,
				CfaStat redirection
		);

		double getSlipFactor(CfaReadableMotionData data);

		void performJump(
				CfaTravelData data,
				CfaStat jumpVel,
				@Nullable CfaStat speedAddend
		);
	}
}
