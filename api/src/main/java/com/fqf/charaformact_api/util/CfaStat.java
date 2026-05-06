package com.fqf.charaformact_api.util;

import com.fqf.charaformact_api.cfadata.CfaData;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Represents a numerical value that might vary depending on character and form.
 * This is used for movement, speed thresholds, and damage calculations.
 * It's kind of like the idea of attribute modifiers, but more granular.
 */
public class CfaStat {
	public static final CfaStat ZERO = new CfaStat(0);

	public final double BASE_VALUE;
	public final EnumSet<StatCategory> CATEGORIES;

	public CfaStat(double baseValue, StatCategory... categories) {
		this.BASE_VALUE = baseValue;
		this.CATEGORIES = EnumSet.noneOf(StatCategory.class);
		this.CATEGORIES.addAll(Arrays.asList(categories));
	}
	public CfaStat variate(double multiplier) {
		return new CfaStat(this.BASE_VALUE * multiplier, this.CATEGORIES);
	}
	public CfaStat variateAndReplaceCategories(double multiplier, StatCategory... categories) {
		return new CfaStat(this.BASE_VALUE * multiplier, categories);
	}
	public CfaStat variateAndAddCategories(double multiplier, StatCategory... extraCategories) {
		EnumSet<StatCategory> newCategorySet = EnumSet.copyOf(this.CATEGORIES);
		newCategorySet.addAll(Arrays.asList(extraCategories));
		return new CfaStat(this.BASE_VALUE * multiplier, newCategorySet);
	}

	private CfaStat(double base, EnumSet<StatCategory> categorySet) {
		this.BASE_VALUE = base;
		this.CATEGORIES = categorySet;
	}

	public double get(CfaData data) {
		return data.getStat(this);
	}
	public double getAsThreshold(CfaData data) {
		return this.get(data) * 0.96;
	}
	public double getAsSquaredThreshold(CfaData data) {
		double threshold = this.getAsThreshold(data);
		return threshold * threshold;
	}
	public double getAsLimit(CfaData data) {
		return this.get(data) * 1.015;
	}
}
