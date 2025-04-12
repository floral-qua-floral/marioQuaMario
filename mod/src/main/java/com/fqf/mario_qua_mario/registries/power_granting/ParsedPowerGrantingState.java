package com.fqf.mario_qua_mario.registries.power_granting;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario_api.definitions.states.StatAlteringStateDefinition;
import com.fqf.mario_qua_mario.registries.ParsedMarioState;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_api.util.StatCategory;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ParsedPowerGrantingState extends ParsedMarioState {
	public final float WIDTH_FACTOR;
	public final float HEIGHT_FACTOR;
	public final float ANIMATION_WIDTH_FACTOR;
	public final float ANIMATION_HEIGHT_FACTOR;

	public final int BUMP_STRENGTH_MODIFIER;
	public final Identifier RESOURCE_ID;

	private final Set<StatAlteringStateDefinition.StatModifier> STAT_MODIFIERS;
	private final Map<Set<StatCategory>, Double> STAT_MULTIPLIERS_CACHE = new HashMap<>();

	public final Set<String> POWERS;

	public ParsedPowerGrantingState(StatAlteringStateDefinition definition) {
		super(definition);
		this.RESOURCE_ID = this.ID.getNamespace().equals("mqm") ? MarioQuaMario.makeResID(this.ID.getPath()) : this.ID;

		this.WIDTH_FACTOR = definition.getWidthFactor();
		this.HEIGHT_FACTOR = definition.getHeightFactor();
		this.ANIMATION_WIDTH_FACTOR = definition.getAnimationWidthFactor();
		this.ANIMATION_HEIGHT_FACTOR = definition.getAnimationHeightFactor();

		this.BUMP_STRENGTH_MODIFIER = definition.getBumpStrengthModifier();

		this.STAT_MODIFIERS = definition.getStatModifiers();

		this.POWERS = definition.getPowers();
	}

	public double adjustStat(CharaStat stat, double startingValue) {
		for(StatAlteringStateDefinition.StatModifier modifier : this.STAT_MODIFIERS) {
			if(stat.CATEGORIES.containsAll(modifier.match()))
				startingValue = modifier.operation().modify(startingValue, stat.CATEGORIES);
		}

		return startingValue;
	}
}
