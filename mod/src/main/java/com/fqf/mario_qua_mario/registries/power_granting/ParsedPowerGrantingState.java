package com.fqf.mario_qua_mario.registries.power_granting;

import com.fqf.mario_qua_mario.definitions.states.MarioStateDefinition;
import com.fqf.mario_qua_mario.definitions.states.StatAlteringStateDefinition;
import com.fqf.mario_qua_mario.registries.ParsedMarioThing;

import java.util.HashSet;
import java.util.Set;

public class ParsedPowerGrantingState extends ParsedMarioThing {
	public final float WIDTH_FACTOR;
	public final float HEIGHT_FACTOR;

	public final int BUMP_STRENGTH_MODIFIER;

	private final Set<StatAlteringStateDefinition.StatModifier> STAT_MODIFIERS;

	public final Set<String> POWERS;

	public ParsedPowerGrantingState(StatAlteringStateDefinition definition) {
		super(definition);

		this.WIDTH_FACTOR = definition.getWidthFactor();
		this.HEIGHT_FACTOR = definition.getHeightFactor();

		this.BUMP_STRENGTH_MODIFIER = definition.getBumpStrengthModifier();

		this.STAT_MODIFIERS = definition.getStatModifiers();

		this.POWERS = definition.getPowers();
	}
}
