package com.fqf.mario_qua_mario.definitions.states.actions.util;

import net.minecraft.util.Identifier;

public record TransitionInjectionDefinition(
		InjectionPlacement placement,
		Identifier injectNearTransitionsTo,
		ActionCategory category,
		TransitionCreator injectedTransitionCreator
) {
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
