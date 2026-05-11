package com.fqf.charaformact.util;

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
	public static FloatFloatImmutablePair getArmorAndToughness(ItemStack equipmentStack, EquipmentSlot belongsInSlot) {
		float armor = 0.0F;
		float toughness = 0.0F;
		boolean attributeModifierFound = false;
		AttributeModifierSlot equippedInSlot = AttributeModifierSlot.forEquipmentSlot(belongsInSlot);

		// This will only find anything if an item has been given attribute modifiers as an item component, such as
		// by commands. It doesn't find anything for items that have them by default (i.e. diamond boots pulled from
		// the creative menu).
		// For testing: /give @s golden_boots[attribute_modifiers={modifiers:[{type:"generic.armor",amount:30,id:test_attribute,operation:add_value}]}]
		// For testing: /give @s golden_boots[attribute_modifiers={modifiers:[{type:"generic.armor",amount:30,slot:armor,id:test_attribute,operation:add_value}]}]
		// For testing: /give @s golden_boots[attribute_modifiers={modifiers:[{type:"generic.armor",amount:30,slot:feet,id:test_attribute,operation:add_value}]}]
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

		// It seems like adding Attribute Modifiers manually to an item that naturally has them causes those natural
		// modifiers to go away?? So only try to find those natural modifiers if we didn't find any custom ones added
		// via an item component. This feels unreliable but I'm not sure how else to do it.
		if(!attributeModifierFound && equipmentStack.getItem() instanceof ArmorItem trueArmor) {
			armor = trueArmor.getProtection();
			toughness = trueArmor.getToughness();
		}

		return new FloatFloatImmutablePair(armor, toughness);
	}
}
