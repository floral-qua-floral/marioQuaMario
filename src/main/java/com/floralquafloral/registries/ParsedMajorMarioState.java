package com.floralquafloral.registries;

import com.floralquafloral.CharaStat;

import java.util.EnumMap;

public abstract class ParsedMajorMarioState extends ParsedMarioState {
	public final float WIDTH_FACTOR;
	public final float HEIGHT_FACTOR;
	public final EnumMap<CharaStat, Double> STAT_FACTORS;

	protected ParsedMajorMarioState(MarioMajorStateDefinition definition) {
		super(definition);

		this.WIDTH_FACTOR = definition.getWidthFactor();
		this.HEIGHT_FACTOR = definition.getHeightFactor();
		this.STAT_FACTORS = new EnumMap<>(CharaStat.class);
		definition.populateStatFactors(this.STAT_FACTORS);
	}
}
