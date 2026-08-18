package com.fqf.charaformact.cfadata.equipment;

import net.minecraft.entity.EquipmentSlot;

public class RenderedArmorInfo {
	private static final EquipmentSlot[] PLAYER_ARMOR_SLOTS =
			{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	public static void register() {
		UpdateEquipmentRenderingCallback.EVENT.register(player -> {
			for(EquipmentSlot slot : PLAYER_ARMOR_SLOTS) {
				player.cfa$getCfaData2().getEquipmentData().updateRenderedEquipmentInfo(
						from(slot),
						player.getEquippedStack(slot),
						RenderedEquipmentInfo::new
				);
			}
		});
	}

	public static VisibleEquipmentSlot from(EquipmentSlot vanillaSlot) {
		return switch (vanillaSlot) {
			case HEAD -> VisibleEquipmentSlot.VANILLA_HELMET;
			case CHEST -> VisibleEquipmentSlot.VANILLA_CHESTPLATE;
			case LEGS -> VisibleEquipmentSlot.VANILLA_LEGGINGS;
			case FEET -> VisibleEquipmentSlot.VANILLA_BOOTS;
			default -> throw new IllegalArgumentException("There is no VisibleEquipmentSlot for " + vanillaSlot + "!");
		};
	}
}
