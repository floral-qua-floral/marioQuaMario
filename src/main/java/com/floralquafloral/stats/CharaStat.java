package com.floralquafloral.stats;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.registries.states.ParsedMajorMarioState;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffects;

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

	private static final double MOVEMENT_SPEED_MULTIPLIER = 1.0 / 0.10000000149011612;
	private static final double MOVEMENT_SPEED_MULTIPLIER_SPRINTING = 1.0 / 0.13000000312924387;

	public double get(MarioData data) {
		double modifiedBase;

		if(data.getMario().isMainPlayer()) { // Only cache stat values for the client-side player
			Double value = CACHE.get(this);
			if(value == null) {
				value = this.BASE * this.getMultiplier(data);
				CACHE.put(this, value);
			}

			modifiedBase = value;
		}
		else modifiedBase = this.BASE * this.getMultiplier(data);

		double attributeFactor = 1.0;
		double attributeAddend = 0.0;

		if(this.CATEGORIES.contains(StatCategory.SPEED) && (
				this.CATEGORIES.contains(StatCategory.WALKING)
						|| this.CATEGORIES.contains(StatCategory.RUNNING)
						|| this.CATEGORIES.contains(StatCategory.P_RUNNING)
						|| this.CATEGORIES.contains(StatCategory.DUCKING)
		)) {
			attributeFactor = data.getMario().getAttributes().getValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)
					* (data.getMario().isSprinting() ? MOVEMENT_SPEED_MULTIPLIER_SPRINTING : MOVEMENT_SPEED_MULTIPLIER);
		}

		if(this.CATEGORIES.contains(StatCategory.JUMP_VELOCITY)) {
			attributeAddend = data.getMario().getJumpBoostVelocityModifier();
		}

		return attributeFactor * modifiedBase + attributeAddend;
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
