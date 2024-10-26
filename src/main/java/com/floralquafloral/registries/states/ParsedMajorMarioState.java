package com.floralquafloral.registries.states;

import com.floralquafloral.stats.StatCategory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class ParsedMajorMarioState extends ParsedMarioState {
	public final float WIDTH_FACTOR;
	public final float HEIGHT_FACTOR;
	public final Map<Set<StatCategory>, Double> STAT_MODIFIERS;

	protected ParsedMajorMarioState(MarioMajorStateDefinition definition) {
		super(definition);

		this.WIDTH_FACTOR = definition.getWidthFactor();
		this.HEIGHT_FACTOR = definition.getHeightFactor();
		this.STAT_MODIFIERS = new HashMap<>();
		definition.populateStatModifiers(this.STAT_MODIFIERS);
//		this.STAT_FACTORS = new EnumMap<>(BaseStats.class);
//		definition.populateStatFactors(this.STAT_FACTORS);
	}
}
