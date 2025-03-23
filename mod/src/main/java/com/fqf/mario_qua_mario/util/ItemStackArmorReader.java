package com.fqf.mario_qua_mario.util;

import it.unimi.dsi.fastutil.floats.FloatFloatImmutablePair;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;

public class ItemStackArmorReader {
	public static FloatFloatImmutablePair read(ItemStack equipmentStack, EquipmentSlot belongsInSlot) {
		float armor = 0.0F;
		float toughness = 0.0F;
		boolean attributeModifierFound = false;
		AttributeModifierSlot equippedInSlot = AttributeModifierSlot.forEquipmentSlot(belongsInSlot);

		for(AttributeModifiersComponent.Entry entry : equipmentStack.getOrDefault(
				DataComponentTypes.ATTRIBUTE_MODIFIERS, AttributeModifiersComponent.DEFAULT).modifiers()) {
			attributeModifierFound = true;
			if(
					entry.modifier().operation() == EntityAttributeModifier.Operation.ADD_VALUE &&
					(entry.slot() == AttributeModifierSlot.ANY
					|| entry.slot() == AttributeModifierSlot.ARMOR
					|| entry.slot() == equippedInSlot)
			) {
				if (entry.attribute().value().equals(EntityAttributes.GENERIC_ARMOR.value()))
					armor += (float) entry.modifier().value();
				else if (entry.attribute().value().equals(EntityAttributes.GENERIC_ARMOR_TOUGHNESS.value()))
					toughness += (float) entry.modifier().value();
			}
		}

		if(!attributeModifierFound && equipmentStack.getItem() instanceof ArmorItem trueArmor) {
			armor = trueArmor.getProtection();
			toughness = trueArmor.getToughness();
		}

		return new FloatFloatImmutablePair(armor, toughness);
	}
}
