package com.floralquafloral.registries;

import com.floralquafloral.CharaStat;

import java.util.EnumMap;

public interface StatChangingStateDefinition {
	void populateStatFactors(EnumMap<CharaStat, Double> statFactorMap);
}
