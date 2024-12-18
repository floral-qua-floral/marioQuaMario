package com.fqf.mario_qua_mario.definitions.states;

import com.fqf.mario_qua_mario.util.StatCategory;

import java.util.Set;

public interface StatAlteringStateDefinition extends MarioStateDefinition {
	float getWidthFactor();
	float getHeightFactor();

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
