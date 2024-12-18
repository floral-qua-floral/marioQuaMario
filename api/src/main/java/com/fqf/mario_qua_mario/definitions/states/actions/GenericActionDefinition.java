package com.fqf.mario_qua_mario.definitions.states.actions;

import com.fqf.mario_qua_mario.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface GenericActionDefinition extends IncompleteActionDefinition {
	void travelHook(IMarioTravelData data);

	@NotNull List<TransitionDefinition> getBasicTransitions();
	@NotNull List<TransitionDefinition> getInputTransitions();
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions();
}
