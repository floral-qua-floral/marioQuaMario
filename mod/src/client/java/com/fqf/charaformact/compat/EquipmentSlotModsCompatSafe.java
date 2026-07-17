package com.fqf.charaformact.compat;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.cfadata.modesty.RenderedArmorInfo;
import com.fqf.charaformact.cfadata.modesty.RenderedEquipmentInfo;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;

import static com.fqf.charaformact_api.cfadata.util.EquipmentCoverSpot.*;

public class EquipmentSlotModsCompatSafe {
	public static final boolean ACCESSORIES_PRESENT = FabricLoader.getInstance().isModLoaded("accessories");

	public static void register() {
		RenderedArmorInfo.register();

		if(ACCESSORIES_PRESENT) {
			CharaFormAct.LOGGER.info("Accessories is loaded! Establishing compatibility...");
			AccessoriesCompatUnsafe.register();
		}
	}



	public static abstract class RenderedModdedSlotInfo extends RenderedEquipmentInfo {
		protected RenderedModdedSlotInfo(ItemStack stack, String slotName) {
			super(stack);
			switch(slotName) {
				case "head" -> {} // TODO: Use armor slot logic!

				case "hat" -> this.tryCover(HEADGEAR, SCALP);
				case "face" -> this.tryCover(HEADGEAR, FACE);
				case "ears" -> this.tryCover(HEADGEAR, EARS);

				case "back", "cape" -> this.tryCover(BACK);
				case "belt" -> this.tryCover(BELLY);

				case "glove" -> this.tryCover(HANDS);

				case "shoes" -> this.tryCover(TOES);
			}
		}
	}
}
