package com.fqf.charaformact_api.definitions.states.actions;

import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charaformact_api.util.CfaStat;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface AirborneActionDefinition extends IncompleteActionDefinition {
	void travelHook(CfaTravelData data, AirborneActionHelper helper);

	default void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {

	}
	default void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {

	}
	default void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {

	}

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

		ActionTransitionDetails makeJumpCapTransition(double capThreshold);
		ActionTransitionDetails makeJumpCapTransition(Identifier postCapAction, double capThreshold);
	}
}
