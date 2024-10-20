package com.floralquafloral.registries;

import com.floralquafloral.CharaStat;

import java.util.EnumMap;

public interface MarioMajorStateDefinition extends MarioStateDefinition {
	void populateStatFactors(EnumMap<CharaStat, Double> statFactorMap);

	float getWidthFactor();
	float getHeightFactor();
}
