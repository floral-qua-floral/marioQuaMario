package com.floralquafloral.registries.states;

import com.floralquafloral.stats.StatCategory;

import java.util.Map;
import java.util.Set;

public interface MarioMajorStateDefinition extends MarioStateDefinition {
	void populateStatModifiers(Map<Set<StatCategory>, Double> modifiers);

	int getBumpStrengthModifier();
	float getWidthFactor();
	float getHeightFactor();
}
