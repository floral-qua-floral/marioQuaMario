package com.floralquafloral.registries.states;

import com.floralquafloral.stats.BaseStats;

import java.util.EnumMap;

public abstract class ParsedMajorMarioState extends ParsedMarioState {
	public final float WIDTH_FACTOR;
	public final float HEIGHT_FACTOR;
	public final EnumMap<BaseStats, Double> STAT_FACTORS;

	protected ParsedMajorMarioState(MarioMajorStateDefinition definition) {
		super(definition);

		this.WIDTH_FACTOR = definition.getWidthFactor();
		this.HEIGHT_FACTOR = definition.getHeightFactor();
		this.STAT_FACTORS = new EnumMap<>(BaseStats.class);
		definition.populateStatFactors(this.STAT_FACTORS);
	}
}
