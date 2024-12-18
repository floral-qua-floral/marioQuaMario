package com.fqf.mario_qua_mario.definitions.states.actions;

import com.fqf.mario_qua_mario.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.CharaStat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AirborneActionDefinition extends IncompleteActionDefinition {
	void travelHook(IMarioTravelData data, AirborneActionHelper helper);

	@NotNull List<TransitionDefinition> getBasicTransitions(AirborneActionHelper helper);
	@NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper);
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper);

	/**
	 * Contains a number of methods intended to help with the creation of Airborne Actions.
	 */
	interface AirborneActionHelper {
		void applyGravity(
				IMarioTravelData data,
				CharaStat gravity, @Nullable CharaStat jumpingGravity,
				CharaStat terminalVelocity
		);

		void airborneAccel(
				IMarioTravelData data,
				CharaStat forwardAccelStat, CharaStat forwardSpeedStat,
				CharaStat backwardAccelStat, CharaStat backwardSpeedStat,
				CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
				double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
		);

		TransitionDefinition makeJumpCapTransition(IncompleteActionDefinition forAction, double capThreshold);
	}
}
