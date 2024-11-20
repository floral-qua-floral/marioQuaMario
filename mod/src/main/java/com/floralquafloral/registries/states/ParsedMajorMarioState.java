package com.floralquafloral.registries.states;

import com.floralquafloral.stats.CharaStat;
import com.floralquafloral.stats.StatCategory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class ParsedMajorMarioState extends ParsedMarioState {
	public final int BUMP_STRENGTH_MODIFIER;
	public final float WIDTH_FACTOR;
	public final float HEIGHT_FACTOR;
	public final Map<Set<StatCategory>, Double> STAT_MODIFIERS;

	private final Map<CharaStat, Double> STAT_MULTIPLIERS_CACHE = new HashMap<>();

	public double getStatMultiplier(CharaStat stat) {
		if(this.STAT_MULTIPLIERS_CACHE.containsKey(stat)) return this.STAT_MULTIPLIERS_CACHE.get(stat);

		double combinedModifier = 1.0;
		for(Map.Entry<Set<StatCategory>, Double> entry : this.STAT_MODIFIERS.entrySet()) {
			if(stat.CATEGORIES.containsAll(entry.getKey()))
				combinedModifier *= entry.getValue();
		}

		this.STAT_MULTIPLIERS_CACHE.put(stat, combinedModifier);
		return combinedModifier;
	}

	protected ParsedMajorMarioState(MarioMajorStateDefinition definition) {
		super(definition);

		this.BUMP_STRENGTH_MODIFIER = definition.getBumpStrengthModifier();
		this.WIDTH_FACTOR = definition.getWidthFactor();
		this.HEIGHT_FACTOR = definition.getHeightFactor();
		this.STAT_MODIFIERS = new HashMap<>();
		definition.populateStatModifiers(this.STAT_MODIFIERS);
//		this.STAT_FACTORS = new EnumMap<>(BaseStats.class);
//		definition.populateStatFactors(this.STAT_FACTORS);
	}
}
