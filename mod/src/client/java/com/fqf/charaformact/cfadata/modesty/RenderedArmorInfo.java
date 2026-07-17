package com.fqf.charaformact.cfadata.modesty;

import com.fqf.charaformact_api.cfadata.util.EquipmentCoverSpot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import static com.fqf.charaformact_api.cfadata.util.EquipmentCoverSpot.*;
import static com.fqf.charaformact_api.cfadata.util.EquipmentCoverSpot.TOES;

public class RenderedArmorInfo extends RenderedEquipmentInfo {
	private static final EquipmentSlot[] PLAYER_ARMOR_SLOTS =
			{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	public static void register() {
		RenderedEquipmentInfo.UPDATE_EQUIPMENT_RENDERING.register(player -> {
			for(EquipmentSlot slot : PLAYER_ARMOR_SLOTS) {
				player.cfa$getCfaData2().getModestyData().updateRenderedEquipmentInfo(slot, player.getEquippedStack(slot), RenderedArmorInfo::new);
			}
		});
	}

	private final EquipmentSlot SLOT;

	public RenderedArmorInfo(ItemStack stack, EquipmentSlot slot) {
		super(stack);
		this.SLOT = slot;
		if(!this.STACK.isEmpty()) switch(this.SLOT) {
			case HEAD -> this.tryCover(HEADGEAR, SCALP, FACE, EARS);
			case CHEST -> this.tryCover(UPPER_CHEST, BELLY, BACK, SHOULDERS, HANDS);
			case LEGS -> this.tryCover(BUTT, TOES);
			case FEET -> this.tryCover(TOES);
		}
	}

	@Override
	protected boolean canCoverSpot(ItemStack stack, EquipmentCoverSpot spot) {
		return PlayerModestyData.doesArmorSlotItemCoverSpot(stack, this.SLOT, spot);
	}
}
