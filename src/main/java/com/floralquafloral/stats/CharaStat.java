package com.floralquafloral.stats;

import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.registries.states.ParsedMajorMarioState;

import java.util.*;

public class CharaStat {
	private final double BASE;
	private final Set<StatCategory> CATEGORIES;

	private static final Map<CharaStat, Double> CACHE = new HashMap<>();

	public static void invalidateCache() {
		CACHE.clear();
	}

	public CharaStat(double base, StatCategory... categories) {
		this.BASE = base;
		this.CATEGORIES = Set.of(categories);
	}

	public double get(MarioData data) {
		if(data.getMario().isMainPlayer()) { // Only cache stat values for the client-side player
			Double value = CACHE.get(this);
			if(value == null) {
				value = this.BASE * this.getMultiplier(data);
				CACHE.put(this, value);
			}

			return value;
		}
		else return this.BASE * this.getMultiplier(data);
	}
	public double getAsThreshold(MarioData data) {
		return this.get(data) * 0.99;
	}
	public double getAsLimit(MarioData data) {
		return this.get(data) * 1.015;
	}

	public double getMultiplier(MarioData data) {
		double multiplier = getSpecificMultiplier(data.getPowerUp());
		if(MarioDataManager.useCharacterStats)
			multiplier *= getSpecificMultiplier(data.getCharacter());
		return multiplier;
	}

	private double getSpecificMultiplier(ParsedMajorMarioState state) {
		double combinedModifier = 1.0;
		for(Map.Entry<Set<StatCategory>, Double> entry : state.STAT_MODIFIERS.entrySet()) {
			if(CATEGORIES.containsAll(entry.getKey()))
				combinedModifier *= entry.getValue();
		}

		return combinedModifier;
	}
}
