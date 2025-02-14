package com.fqf.mario_qua_mario.definitions.states.actions.util;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

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
		TransitionDefinition makeTransition(TransitionDefinition nearbyTransition, CastableHelper castableHelper);

		interface CastableHelper {

		}
	}

	public enum InjectionPlacement {
		BEFORE,
		AFTER
	}
}
