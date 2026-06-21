package com.fqf.charaformact_api.definitions.states.actions;

import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.util.CfaStat;
import com.google.common.collect.ImmutableList;

public interface AquaticActionDefinition extends IncompleteActionDefinition {
	void travelHook(CfaTravelData data, AquaticActionHelper helper);

	default void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AquaticActionHelper helper) {

	}
	default void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AquaticActionHelper helper) {

	}
	default void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AquaticActionHelper helper) {

	}

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
