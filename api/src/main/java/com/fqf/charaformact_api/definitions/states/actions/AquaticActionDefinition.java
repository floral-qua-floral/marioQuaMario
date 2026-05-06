package com.fqf.charaformact_api.definitions.states.actions;

import com.fqf.charaformact_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.util.CfaStat;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface AquaticActionDefinition extends IncompleteActionDefinition {
	void travelHook(CfaTravelData data, AquaticActionHelper helper);

	@NotNull List<TransitionDefinition> getBasicTransitions(AquaticActionHelper helper);
	@NotNull List<TransitionDefinition> getInputTransitions(AquaticActionHelper helper);
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions(AquaticActionHelper helper);

	/**
	 * Contains a number of methods intended to help with the creation of Aquatic Actions.
	 */
	interface AquaticActionHelper {
		void applyGravity(CfaTravelData data, CfaStat gravity, CfaStat terminalVelocity);

		void applyWaterDrag(CfaTravelData data, CfaStat drag, CfaStat dragMin);

		void aquaticAccel(
				CfaTravelData data,
				CfaStat forwardAccelStat, CfaStat forwardSpeedStat,
				CfaStat backwardAccelStat, CfaStat backwardSpeedStat,
				CfaStat strafeAccelStat, CfaStat strafeSpeedStat,
				double forwardAngleContribution, double strafeAngleContribution, CfaStat redirectStat
		);
	}
}
