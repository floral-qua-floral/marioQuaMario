package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.mariodata.IMarioData;

import java.util.Set;

/**
 * Represents a numerical value that might vary depending on character and power-up form.
 * This is used for movement, speed thresholds, and damage calculations.
 */
public class CharaStat {
	public final double BASE;
	public final Set<StatCategory> CATEGORIES;

	public CharaStat(double base, StatCategory... categories) {
		this(base, Set.of(categories));
	}
	public CharaStat variate(double multiplier) {
		return new CharaStat(this.BASE * multiplier, this.CATEGORIES);
	}

	private CharaStat(double base, Set<StatCategory> categorySet) {
		this.BASE = base;
		this.CATEGORIES = categorySet;
	}

	public double get(IMarioData data) {
		return data.getStat(this);
	}
	public double getAsThreshold(IMarioData data) {
		return this.get(data) * 0.96;
	}
	public double getAsLimit(IMarioData data) {
		return this.get(data) * 1.015;
	}
}
