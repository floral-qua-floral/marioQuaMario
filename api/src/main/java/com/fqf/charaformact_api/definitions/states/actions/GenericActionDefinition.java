package com.fqf.charaformact_api.definitions.states.actions;

import com.fqf.charaformact_api.definitions.states.actions.util.GenericActionType;
import com.fqf.charaformact_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface GenericActionDefinition extends IncompleteActionDefinition {
	@NotNull GenericActionType getGenericActionType();

	boolean travelHook(CfaTravelData data);

	@NotNull List<TransitionDefinition> getBasicTransitions();
	@NotNull List<TransitionDefinition> getInputTransitions();
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions();
}
