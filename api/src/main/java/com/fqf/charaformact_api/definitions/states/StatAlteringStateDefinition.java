package com.fqf.charaformact_api.definitions.states;

import com.fqf.charaformact_api.util.StatCategory;
import com.google.common.collect.ImmutableSet;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Set;

public interface StatAlteringStateDefinition extends CfaStateDefinition {
	default float defineWidthFactor() {
		return 1;
	}
	default float defineHeightFactor() {
		return 1;
	}
	default float defineEyeHeightFactor() {
		return this.defineHeightFactor();
	}
	default float defineAnimationHorizontalScale() {
		return 1;
	}
	default float defineAnimationVerticalScale() {
		return 1;
	}

	default int defineBapStrengthModifier() {
		return 0;
	}

	default void accumulatePowers(ImmutableSet.Builder<String> builder) {

	}
	default void accumulateAttributeModifiers(ImmutableSet.Builder<AttributeModifierInstruction> builder) {

	}
	default void accumulateStatModifiers(ImmutableSet.Builder<StatModifier> builder) {

	}

	record AttributeModifierInstruction(
			RegistryEntry<EntityAttribute> attribute,
			double d,
			EntityAttributeModifier.Operation operation
	) {

	}

	record StatModifier(
			Set<StatCategory> match,
			StatOperation operation
	) {
		public StatModifier(Set<StatCategory> match, double factor) {
			this(match, (base, categories) -> base * factor);
		}

		@FunctionalInterface public interface StatOperation {
			double modify(double base, Set<StatCategory> categories);
		}
	}
}
