package com.fqf.charaformact.cfadata.modesty;

import com.fqf.charaformact_api.cfadata.util.EquipmentCoverSpot;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

import java.util.EnumSet;
import java.util.Set;

import static com.fqf.charaformact_api.cfadata.util.EquipmentCoverSpot.*;

public class RenderedArmorInfo {
	public final ItemStack STACK;
	public final Set<EquipmentCoverSpot> COVER_SPOTS;

	public RenderedArmorInfo(ItemStack stack, EquipmentSlot slot) {
		this.STACK = stack;
		this.COVER_SPOTS = EnumSet.noneOf(EquipmentCoverSpot.class);
		if(!this.STACK.isEmpty()) switch(slot) {
			case HEAD -> this.tryCover(slot, HEADGEAR, SCALP, FACE, EARS);
			case CHEST -> this.tryCover(slot, UPPER_CHEST, BELLY, BACK, SHOULDERS, HANDS);
			case LEGS -> this.tryCover(slot, BUTT, TOES);
			case FEET -> this.tryCover(slot, TOES);
		}
	}

	private void tryCover(EquipmentSlot slot, EquipmentCoverSpot... spots) {
		for (EquipmentCoverSpot spot : spots)
			if (PlayerModestyData.doesArmorSlotItemCoverSpot(this.STACK, slot, spot))
				this.COVER_SPOTS.add(spot);
	}
}
