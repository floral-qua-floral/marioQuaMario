package com.fqf.charaformact.util;

import it.unimi.dsi.fastutil.floats.FloatFloatImmutablePair;
import it.unimi.dsi.fastutil.floats.FloatFloatMutablePair;
import it.unimi.dsi.fastutil.floats.FloatFloatPair;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.function.BiConsumer;

public class ItemStackArmorReader {
	public static FloatFloatPair getArmorAndToughness(ItemStack equipmentStack, EquipmentSlot belongsInSlot) {
		final FloatFloatMutablePair armorAndToughness = new FloatFloatMutablePair(0, 0);

		BiConsumer<RegistryEntry<EntityAttribute>, EntityAttributeModifier> attributeModifierConsumer = (attribute, modifier) -> {
			// This can't handle multiplicative armor items.
			// But like does any mod even do that??
			if(modifier.operation() == EntityAttributeModifier.Operation.ADD_VALUE) {
				if(attribute == EntityAttributes.GENERIC_ARMOR)
					armorAndToughness.left(armorAndToughness.leftFloat() + (float) modifier.value());
				else if(attribute == EntityAttributes.GENERIC_ARMOR_TOUGHNESS)
					armorAndToughness.right(armorAndToughness.rightFloat() + (float) modifier.value());
			}
		};

		equipmentStack.applyAttributeModifiers(belongsInSlot, attributeModifierConsumer);

		return armorAndToughness;
	}
}
