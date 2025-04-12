package com.fqf.mario_qua_mario_api.definitions.states.actions;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface AquaticActionDefinition extends IncompleteActionDefinition {
	void travelHook(IMarioTravelData data, AquaticActionHelper helper);

	@NotNull List<TransitionDefinition> getBasicTransitions(AquaticActionHelper helper);
	@NotNull List<TransitionDefinition> getInputTransitions(AquaticActionHelper helper);
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions(AquaticActionHelper helper);

	/**
	 * Contains a number of methods intended to help with the creation of Aquatic Actions.
	 */
	interface AquaticActionHelper {
		void applyGravity(IMarioTravelData data, CharaStat gravity, CharaStat terminalVelocity);

		void applyWaterDrag(IMarioTravelData data, CharaStat drag, CharaStat dragMin);

		void aquaticAccel(
				IMarioTravelData data,
				CharaStat forwardAccelStat, CharaStat forwardSpeedStat,
				CharaStat backwardAccelStat, CharaStat backwardSpeedStat,
				CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
				double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
		);
	}
}
