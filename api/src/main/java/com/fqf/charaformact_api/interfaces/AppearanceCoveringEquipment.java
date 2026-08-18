package com.fqf.charaformact_api.interfaces;

import com.fqf.charaformact_api.cfadata.util.EquipmentCoverSpot;
import com.google.common.collect.ImmutableSet;
import net.minecraft.item.ItemStack;

/**
 * Classes extending Item can implement this to take finer control over how the item covers up Appearance models when
 * equipped. Override the method and make it insert ALL the EquipmentCoverSpots that the item should cover up! This
 * replaces the tag-based covering system for this item.
 * Appearance covering is used for rendering, not mechanics, so equipment worn in purely cosmetic slots are able to
 * cover up Appearance models. Items must be equipped in either one of the four vanilla armor slots or in an Accessory
 * slot to interact with Appearance covering.
 * <p>
 * Please be aware that accumulateCoveringSpots is called once when the item begins being rendered, and is not called
 * again unless the item is taken off and put back on. As such, it does not support changing over time.
 */
public interface AppearanceCoveringEquipment {
	void accumulateCoveringSpots(ItemStack itemStack, ImmutableSet.Builder<EquipmentCoverSpot> builder);
}
