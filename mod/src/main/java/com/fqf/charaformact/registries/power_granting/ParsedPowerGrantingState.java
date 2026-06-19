package com.fqf.charaformact.registries.power_granting;

import com.fqf.charaformact_api.definitions.states.StatAlteringStateDefinition;
import com.fqf.charaformact.registries.ParsedCfaState;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.StatCategory;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ParsedPowerGrantingState extends ParsedCfaState {
	public final float WIDTH_FACTOR;
	public final float HEIGHT_FACTOR;
	public final float EYE_HEIGHT_FACTOR;
	public final float ANIMATION_HORIZONTAL_SCALE;
	public final float ANIMATION_VERTICAL_SCALE;

	public final int BUMP_STRENGTH_MODIFIER;
	public final Identifier RESOURCE_ID;


	public final Set<String> POWERS;
	public final Set<StatAlteringStateDefinition.AttributeModifierInstruction> ATTRIBUTE_MODIFIERS;
	private final Set<StatAlteringStateDefinition.StatModifier> STAT_MODIFIERS;
	private final Map<Set<StatCategory>, Double> STAT_MULTIPLIERS_CACHE = new HashMap<>();

	public ParsedPowerGrantingState(StatAlteringStateDefinition definition) {
		super(definition);
		this.RESOURCE_ID = this.ID;

		this.WIDTH_FACTOR = definition.defineWidthFactor();
		this.HEIGHT_FACTOR = definition.defineHeightFactor();
		this.EYE_HEIGHT_FACTOR = definition.defineEyeHeightFactor();
		this.ANIMATION_HORIZONTAL_SCALE = definition.defineAnimationHorizontalScale();
		this.ANIMATION_VERTICAL_SCALE = definition.defineAnimationVerticalScale();

		this.BUMP_STRENGTH_MODIFIER = definition.defineBapStrengthModifier();

		this.POWERS = accumulateSet(definition::accumulatePowers);
		this.ATTRIBUTE_MODIFIERS = accumulateSet(definition::accumulateAttributeModifiers);
		this.STAT_MODIFIERS = accumulateSet(definition::accumulateStatModifiers);
	}

	public double adjustStat(CfaStat stat, double startingValue) {
		for(StatAlteringStateDefinition.StatModifier modifier : this.STAT_MODIFIERS) {
			if(stat.CATEGORIES.containsAll(modifier.match()))
				startingValue = modifier.operation().modify(startingValue, stat.CATEGORIES);
		}

		return startingValue;
	}
}
