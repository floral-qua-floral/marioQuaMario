package com.fqf.mario_qua_mario_api.util;

import com.fqf.mario_qua_mario_api.mariodata.IMarioData;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * Represents a numerical value that might vary depending on character and power-up form.
 * This is used for movement, speed thresholds, and damage calculations.
 */
public class CharaStat {
	public static final CharaStat ZERO = new CharaStat(0);

	public final double BASE_VALUE;
	public final EnumSet<StatCategory> CATEGORIES;

	public CharaStat(double baseValue, StatCategory... categories) {
		this.BASE_VALUE = baseValue;
		this.CATEGORIES = EnumSet.noneOf(StatCategory.class);
		this.CATEGORIES.addAll(Arrays.asList(categories));
	}
	public CharaStat variate(double multiplier) {
		return new CharaStat(this.BASE_VALUE * multiplier, this.CATEGORIES);
	}
	public CharaStat variateAndReplaceCategories(double multiplier, StatCategory... categories) {
		return new CharaStat(this.BASE_VALUE * multiplier, categories);
	}
	public CharaStat variateAndAddCategories(double multiplier, StatCategory... extraCategories) {
		EnumSet<StatCategory> newCategorySet = EnumSet.copyOf(this.CATEGORIES);
		newCategorySet.addAll(Arrays.asList(extraCategories));
		return new CharaStat(this.BASE_VALUE * multiplier, newCategorySet);
	}

	private CharaStat(double base, EnumSet<StatCategory> categorySet) {
		this.BASE_VALUE = base;
		this.CATEGORIES = categorySet;
	}

	public double get(IMarioData data) {
		return data.getStat(this);
	}
	public double getAsThreshold(IMarioData data) {
		return this.get(data) * 0.96;
	}
	public double getAsSquaredThreshold(IMarioData data) {
		double threshold = this.getAsThreshold(data);
		return threshold * threshold;
	}
	public double getAsLimit(IMarioData data) {
		return this.get(data) * 1.015;
	}
}
