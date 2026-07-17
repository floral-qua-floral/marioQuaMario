package com.fqf.charaformact_api.interfaces;

import com.fqf.charaformact_api.cfadata.util.EquipmentCoverSpot;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;

/**
 * Classes extending Item can implement this to take finer control over how the item covers up Appearance models when
 * equipped. Override the method and make it insert ALL the EquipmentCoverSpots that the item should cover up! This
 * replaces the tag-based covering system for this item.
 * Appearance covering is based on rendering, not mechanics, so equipment worn in purely cosmetic slots are able to
 * cover up Appearance models. Currently, items that are equipped in a vanilla armor slot or an Accessories slot can
 * use Appearance Covering, while other mods (Trinkets, Curios) will not.
 */
public interface AppearanceCoveringEquipment {
	void accumulateCoveringSpots(ItemStack itemStack, ImmutableSet.Builder<EquipmentCoverSpot> builder);
}
