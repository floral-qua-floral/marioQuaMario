package com.fqf.mario_qua_mario.definitions.actions.util;

import net.minecraft.util.Identifier;

public class TransitionInjectionDefinition {
	@FunctionalInterface
	public interface TransitionCreator {
		TransitionDefinition makeTransition(TransitionDefinition previousTransition);
	}

	public enum InjectionPlacement {
		BEFORE,
		AFTER
	}

	public enum ActionCategory {
		ANY,
		GROUNDED,
		AIRBORNE,
		AQUATIC,
		WALL,
		UNDEFINED
	}

	public TransitionInjectionDefinition(
			InjectionPlacement placement,
			String injectNearTransitionsTo,
			ActionCategory category,
			TransitionCreator injectedTransitionCreator
	) {
		this.PLACEMENT = placement;
		this.INJECT_NEAR = Identifier.of(injectNearTransitionsTo);
		this.INJECT_IN_CATEGORY = category;
		this.TRANSITION_CREATOR = injectedTransitionCreator;
	}

	public final InjectionPlacement PLACEMENT;
	public final Identifier INJECT_NEAR;
	public final ActionCategory INJECT_IN_CATEGORY;
	public final TransitionCreator TRANSITION_CREATOR;
}
