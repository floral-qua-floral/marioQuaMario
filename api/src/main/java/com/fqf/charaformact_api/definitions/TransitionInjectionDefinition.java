package com.fqf.charaformact_api.definitions;

import com.fqf.charaformact_api.definitions.states.actions.GenericActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public interface TransitionInjectionDefinition {
	@Nullable InjectionPlacement getPlacementRelativeTo(
			ActionCategory fromCategory, Identifier fromID,
			ActionCategory toCategory, Identifier toID
	);

	@NotNull ActionTransitionDetails makeTransition(ActionTransitionDetails nearbyTransition, GenericActionDefinition.CastableHelper helper);

	record Simple(
			@NotNull InjectionPlacement relativePosition,
			Identifier injectNearTransitionsTo,
			@Nullable ActionCategory onlyFromCategory,
			BiFunction<ActionTransitionDetails, GenericActionDefinition.CastableHelper, ActionTransitionDetails> transitionCreator
	) implements TransitionInjectionDefinition {
		@Override
		public @Nullable InjectionPlacement getPlacementRelativeTo(ActionCategory fromCategory, Identifier fromID, ActionCategory toCategory, Identifier toID) {
			return (
					toID == this.injectNearTransitionsTo
					&& (this.onlyFromCategory == null || fromCategory == this.onlyFromCategory)
			) ? this.relativePosition : null;
		}

		@Override
		public @NotNull ActionTransitionDetails makeTransition(ActionTransitionDetails nearbyTransition, GenericActionDefinition.CastableHelper helper) {
			return this.transitionCreator.apply(nearbyTransition, helper);
		}
	}

	enum InjectionPlacement {
		BEFORE,
		AFTER
	}
}
