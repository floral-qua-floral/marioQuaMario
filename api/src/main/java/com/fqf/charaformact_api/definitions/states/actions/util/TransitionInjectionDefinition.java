package com.fqf.charaformact_api.definitions.states.actions.util;

import com.fqf.charaformact_api.definitions.states.actions.*;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record TransitionInjectionDefinition(
		InjectionPlacement placement,
		Identifier injectNearTransitionsTo,
		@Nullable InjectionPredicate predicate,
		TransitionCreator injectedTransitionCreator
) {
	public TransitionInjectionDefinition(
			InjectionPlacement placement,
			Identifier injectNearTransitionsTo,
			ActionCategory category,
			TransitionCreator injectedTransitionCreator
	) {
		this(placement, injectNearTransitionsTo, (fromAction, fromCategory, existingTransitions) -> fromCategory.equals(category), injectedTransitionCreator);
	}

	@FunctionalInterface
	public interface InjectionPredicate {
		boolean shouldInject(Identifier fromAction, ActionCategory fromCategory, List<Identifier> existingTransitions);
	}

	@FunctionalInterface
	public interface TransitionCreator {
		TransitionDefinition makeTransition(TransitionDefinition nearbyTransition, GenericActionDefinition.CastableHelper castableHelper);
	}

	public enum InjectionPlacement {
		BEFORE,
		AFTER
	}
}
