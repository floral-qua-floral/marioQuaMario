package com.fqf.charaformact_api.definitions.states.actions;

import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.google.common.collect.ImmutableList;

public interface GenericActionDefinition extends IncompleteActionDefinition {

	void travelHook(CfaTravelData data);

	default void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, TransitionHelper helper) {

	}
	default void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, TransitionHelper helper) {

	}
	default void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, TransitionHelper helper) {

	}

	interface TransitionHelper {
		GroundedActionDefinition.GroundedActionHelper asGrounded();
		AirborneActionDefinition.AirborneActionHelper asAirborne();
		AquaticActionDefinition.AquaticActionHelper asAquatic();
		WallboundActionDefinition.WallboundActionHelper asWallbound();
		MountedActionDefinition.MountedActionHelper asMounted();
	}
}
