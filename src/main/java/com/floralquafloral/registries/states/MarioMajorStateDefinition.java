package com.floralquafloral.registries.states;

import com.floralquafloral.stats.BaseStats;

import java.util.EnumMap;

public interface MarioMajorStateDefinition extends MarioStateDefinition {
	void populateStatFactors(EnumMap<BaseStats, Double> statFactorMap);

	float getWidthFactor();
	float getHeightFactor();
}
