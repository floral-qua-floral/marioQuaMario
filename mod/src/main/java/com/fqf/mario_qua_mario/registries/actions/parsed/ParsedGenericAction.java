package com.fqf.mario_qua_mario.registries.actions.parsed;

import com.fqf.mario_qua_mario.definitions.states.actions.GenericActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ParsedGenericAction extends AbstractParsedAction {
	private final GenericActionDefinition GENERIC_DEFINITION;

	public ParsedGenericAction(GenericActionDefinition definition, HashMap<Identifier, Set<TransitionInjectionDefinition>> allInjections) {
		super(definition, allInjections);
		this.GENERIC_DEFINITION = definition;
	}

	@Override
	public boolean travelHook(MarioMoveableData data) {
		this.GENERIC_DEFINITION.travelHook(data);
		return true;
	}

	@Override
	protected TransitionInjectionDefinition.ActionCategory getCategory() {
		return TransitionInjectionDefinition.ActionCategory.GENERIC;
	}

	@Override
	protected List<TransitionDefinition> getBasicTransitions() {
		return this.GENERIC_DEFINITION.getBasicTransitions();
	}

	@Override
	protected List<TransitionDefinition> getInputTransitions() {
		return this.GENERIC_DEFINITION.getInputTransitions();
	}

	@Override
	protected List<TransitionDefinition> getWorldCollisionTransitions() {
		return this.GENERIC_DEFINITION.getWorldCollisionTransitions();
	}
}
