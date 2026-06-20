package com.fqf.charaformact_api.definitions.states.actions;

import com.fqf.charaformact_api.definitions.states.actions.util.GenericActionType;
import com.fqf.charaformact_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

public interface GenericActionDefinition extends IncompleteActionDefinition {
	@NotNull GenericActionType getGenericActionType();

	boolean travelHook(CfaTravelData data);

	default void accumulateBasicTransitions(ImmutableList.Builder<TransitionDefinition> builder, CastableHelper helper) {

	}
	default void accumulateInputTransitions(ImmutableList.Builder<TransitionDefinition> builder, CastableHelper helper) {

	}
	default void accumulateCollisionTransitions(ImmutableList.Builder<TransitionDefinition> builder, CastableHelper helper) {

	}

	interface CastableHelper {
		GroundedActionDefinition.GroundedActionHelper asGrounded();
		AirborneActionDefinition.AirborneActionHelper asAirborne();
		AquaticActionDefinition.AquaticActionHelper asAquatic();
		WallboundActionDefinition.WallboundActionHelper asWallbound();
		MountedActionDefinition.MountedActionHelper asMounted();
	}
}
