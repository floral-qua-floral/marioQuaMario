package com.floralquafloral.stats;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.registries.states.ParsedMajorMarioState;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;

import java.util.*;

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

	public double get(MarioData data) {
		return data.getStat(this);
	}
	public double getAsThreshold(MarioData data) {
		return this.get(data) * 0.96;
	}
	public double getAsLimit(MarioData data) {
		return this.get(data) * 1.015;
	}
}
