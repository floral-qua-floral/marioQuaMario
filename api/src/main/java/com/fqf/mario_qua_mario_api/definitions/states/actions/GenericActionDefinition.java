package com.fqf.mario_qua_mario_api.definitions.states.actions;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.GenericActionType;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.SprintingRule;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface GenericActionDefinition extends IncompleteActionDefinition {
	@NotNull GenericActionType getGenericActionType();

	boolean travelHook(IMarioTravelData data);

	@NotNull List<TransitionDefinition> getBasicTransitions();
	@NotNull List<TransitionDefinition> getInputTransitions();
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions();
}
