package com.fqf.charaformact_api.definitions.states;

import com.fqf.charaformact_api.util.StatCategory;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.Set;

public interface StatAlteringStateDefinition extends CfaStateDefinition {
	float getWidthFactor();
	float getHeightFactor();
	default float getEyeHeightFactor() {
		return this.getHeightFactor();
	}
	float getAnimationHorizontalScale();
	float getAnimationVerticalScale();

	int getBapStrengthModifier();

	Set<String> getPowers();

	Set<AttributeModifierInstruction> getAttributeModifiers();

	Set<StatModifier> getStatModifiers();

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
