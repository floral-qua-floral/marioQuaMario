package com.fqf.charapoweract_api.definitions.states.actions;

import com.fqf.charapoweract_api.definitions.states.actions.util.GenericActionType;
import com.fqf.charapoweract_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charapoweract_api.mariodata.IMarioTravelData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface GenericActionDefinition extends IncompleteActionDefinition {
	@NotNull GenericActionType getGenericActionType();

	boolean travelHook(IMarioTravelData data);

	@NotNull List<TransitionDefinition> getBasicTransitions();
	@NotNull List<TransitionDefinition> getInputTransitions();
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions();
}
