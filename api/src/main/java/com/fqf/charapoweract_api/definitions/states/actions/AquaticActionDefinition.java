package com.fqf.charapoweract_api.definitions.states.actions;

import com.fqf.charapoweract_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charapoweract_api.cpadata.ICPATravelData;
import com.fqf.charapoweract_api.util.CharaStat;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface AquaticActionDefinition extends IncompleteActionDefinition {
	void travelHook(ICPATravelData data, AquaticActionHelper helper);

	@NotNull List<TransitionDefinition> getBasicTransitions(AquaticActionHelper helper);
	@NotNull List<TransitionDefinition> getInputTransitions(AquaticActionHelper helper);
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions(AquaticActionHelper helper);

	/**
	 * Contains a number of methods intended to help with the creation of Aquatic Actions.
	 */
	interface AquaticActionHelper {
		void applyGravity(ICPATravelData data, CharaStat gravity, CharaStat terminalVelocity);

		void applyWaterDrag(ICPATravelData data, CharaStat drag, CharaStat dragMin);

		void aquaticAccel(
				ICPATravelData data,
				CharaStat forwardAccelStat, CharaStat forwardSpeedStat,
				CharaStat backwardAccelStat, CharaStat backwardSpeedStat,
				CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
				double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
		);
	}
}
