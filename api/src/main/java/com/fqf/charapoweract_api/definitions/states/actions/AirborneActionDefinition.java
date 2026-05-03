package com.fqf.charapoweract_api.definitions.states.actions;

import com.fqf.charapoweract_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charapoweract_api.cpadata.ICPATravelData;
import com.fqf.charapoweract_api.util.CharaStat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AirborneActionDefinition extends IncompleteActionDefinition {
	void travelHook(ICPATravelData data, AirborneActionHelper helper);

	@NotNull List<TransitionDefinition> getBasicTransitions(AirborneActionHelper helper);
	@NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper);
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper);

	/**
	 * Contains a number of methods intended to help with the creation of Airborne Actions.
	 */
	interface AirborneActionHelper {
		void applyComplexGravity(
				ICPATravelData data,
				CharaStat gravity, @Nullable CharaStat jumpingGravity,
				CharaStat terminalVelocity
		);

		void airborneAccel(
				ICPATravelData data,
				CharaStat forwardAccelStat, CharaStat forwardSpeedStat,
				CharaStat backwardAccelStat, CharaStat backwardSpeedStat,
				CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
				double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
		);

		TransitionDefinition makeJumpCapTransition(IncompleteActionDefinition forAction, double capThreshold);
	}
}
