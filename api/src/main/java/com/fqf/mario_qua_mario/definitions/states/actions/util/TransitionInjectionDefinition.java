package com.fqf.mario_qua_mario.definitions.states.actions.util;

import com.fqf.mario_qua_mario.definitions.states.actions.*;
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
		TransitionDefinition makeTransition(TransitionDefinition nearbyTransition, CastableHelper castableHelper);

		interface CastableHelper {
			GroundedActionDefinition.GroundedActionHelper asGrounded();
			AirborneActionDefinition.AirborneActionHelper asAirborne();
			AquaticActionDefinition.AquaticActionHelper asAquatic();
			WallboundActionDefinition.WallboundActionHelper asWallbound();
			MountedActionDefinition.MountedActionHelper asMounted();
		}
	}

	public enum InjectionPlacement {
		BEFORE,
		AFTER
	}
}
