package com.fqf.mario_qua_mario.registries.actions;

import com.fqf.mario_qua_mario.definitions.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record ParsedTransition(
		@NotNull ParsedAction targetID,
		@NotNull TransitionDefinition.Evaluator evaluator,
		@Nullable TransitionDefinition.TravelExecutor travelExecutor,
		@Nullable TransitionDefinition.ClientsExecutor clientsExecutor
) {
	public ParsedTransition(TransitionDefinition definition) {
		this(Objects.requireNonNull(RegistryManager.ACTIONS.get(definition.targetID())), definition.evaluator(), definition.travelExecutor(), definition.clientsExecutor());
	}
}
