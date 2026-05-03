package com.fqf.charapoweract_api.definitions.states;

import com.fqf.charapoweract_api.util.StatCategory;

import java.util.Set;

public interface StatAlteringStateDefinition extends CPAStateDefinition {
	float getWidthFactor();
	float getHeightFactor();
	float getAnimationWidthFactor();
	float getAnimationHeightFactor();

	int getBumpStrengthModifier();

	Set<String> getPowers();

	Set<StatModifier> getStatModifiers();

	record StatModifier(
			Set<StatCategory> match,
			StatOperation operation
	) {
		public StatModifier(Set<StatCategory> match, double factor) {
			this(match, (base, categories) -> base * factor);
		}

		@FunctionalInterface public interface StatOperation {
			double modify(double base, Set<StatCategory> categories);
		}
	}
}
