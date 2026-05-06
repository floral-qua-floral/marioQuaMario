package com.fqf.charaformact_api.definitions.states.actions;

import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.util.CfaStat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface AirborneActionDefinition extends IncompleteActionDefinition {
	void travelHook(CfaTravelData data, AirborneActionHelper helper);

	@NotNull List<TransitionDefinition> getBasicTransitions(AirborneActionHelper helper);
	@NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper);
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper);

	/**
	 * Contains a number of methods intended to help with the creation of Airborne Actions.
	 */
	interface AirborneActionHelper {
		void applyComplexGravity(
				CfaTravelData data,
				CfaStat gravity, @Nullable CfaStat jumpingGravity,
				CfaStat terminalVelocity
		);

		void airborneAccel(
				CfaTravelData data,
				CfaStat forwardAccelStat, CfaStat forwardSpeedStat,
				CfaStat backwardAccelStat, CfaStat backwardSpeedStat,
				CfaStat strafeAccelStat, CfaStat strafeSpeedStat,
				double forwardAngleContribution, double strafeAngleContribution, CfaStat redirectStat
		);

		TransitionDefinition makeJumpCapTransition(IncompleteActionDefinition forAction, double capThreshold);
	}
}
