package com.fqf.charaformact.compat;

import com.fqf.charaformact.cfadata.modesty.RenderedEquipmentInfo;
import com.mojang.datafixers.util.Pair;
import io.wispforest.accessories.Accessories;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.AccessoriesContainer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.item.ItemStack;

class AccessoriesCompatUnsafe {
	public static void register() {
		RenderedEquipmentInfo.UPDATE_EQUIPMENT_RENDERING.register(player -> {
			AccessoriesCapability capability = AccessoriesCapability.get(player);
			if(capability != null) {
				for(AccessoriesContainer container : capability.getContainers().values()) {
					for(Pair<Integer, ItemStack> accessory : container.getAccessories()) {
						ItemStack stack = accessory.getSecond();
						SlotReference reference = container.createReference(accessory.getFirst());

						ItemStack cosmetic = container.getCosmeticAccessories().getStack(reference.slot());

						if(!cosmetic.isEmpty() && Accessories.config().clientOptions.showCosmeticAccessories()) stack = cosmetic;

						player.cfa$getCfaData2().getModestyData().updateRenderedEquipmentInfo(
								reference,
								container.shouldRender(accessory.getFirst()) ? stack : ItemStack.EMPTY,
								RenderedAccessoryInfo::new
						);
					}
				}
			}
		});
	}

	private static class RenderedAccessoryInfo extends EquipmentSlotModsCompatSafe.RenderedModdedSlotInfo {
		protected RenderedAccessoryInfo(ItemStack stack, SlotReference slot) {
			super(stack, switch(slot.slotName()) {

				case "hand" -> "glove";
				default -> slot.slotName();
			});
		}
	}
}
