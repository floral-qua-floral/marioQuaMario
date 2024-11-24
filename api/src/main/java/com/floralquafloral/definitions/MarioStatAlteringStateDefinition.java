package com.floralquafloral.definitions;

import com.floralquafloral.definitions.actions.StatCategory;

import java.util.Map;
import java.util.Set;

public interface MarioStatAlteringStateDefinition extends MarioStateDefinition {
	void populateStatModifiers(Map<Set<StatCategory>, Double> modifiers);

	int getBumpStrengthModifier();
	float getWidthFactor();
	float getHeightFactor();
}
