package com.fqf.mario_qua_mario.registries.actions;

import com.fqf.mario_qua_mario.definitions.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.registries.RegistryManager;

public class ParsedTransition {
	public final ParsedAction TARGET;
	public final TransitionDefinition.Evaluator EVALUATOR;
	public final TransitionDefinition.TravelExecutor TRAVEL_EXECUTOR;
	public final TransitionDefinition.ClientsExecutor CLIENTS_EXECUTOR;

	public final int INDEX;

	public ParsedTransition(TransitionDefinition definition, int index) {
		this.TARGET = RegistryManager.ACTIONS.get(definition.TARGET_IDENTIFIER);
		this.EVALUATOR = definition.EVALUATOR;
		this.TRAVEL_EXECUTOR = definition.TRAVEL_EXECUTOR;
		this.CLIENTS_EXECUTOR = definition.CLIENTS_EXECUTOR;

		this.INDEX = index;
	}
}
