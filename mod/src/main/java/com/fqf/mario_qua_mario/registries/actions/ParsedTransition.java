package com.fqf.mario_qua_mario.registries.actions;

import com.fqf.mario_qua_mario.definitions.actions.util.EvaluatorContext;
import com.fqf.mario_qua_mario.definitions.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record ParsedTransition(
		@NotNull AbstractParsedAction targetAction,
		@NotNull TransitionDefinition.Evaluator evaluator,
		boolean fullyNetworked,
		@Nullable TransitionDefinition.TravelExecutor travelExecutor,
		@Nullable TransitionDefinition.ClientsExecutor clientsExecutor
) {
	public ParsedTransition(TransitionDefinition definition) {
		this(
				Objects.requireNonNull(RegistryManager.ACTIONS.get(definition.targetID())),
				definition.evaluator(),
				definition.context() != EvaluatorContext.COMMON,
				definition.travelExecutor(),
				definition.clientsExecutor()
		);
	}
}
