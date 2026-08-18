package com.fqf.charaformact.compat;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.cfadata.equipment.RenderedArmorInfo;
import net.fabricmc.loader.api.FabricLoader;

public class EquipmentSlotModsCompatSafe {
	public static final boolean ACCESSORIES_PRESENT = FabricLoader.getInstance().isModLoaded("accessories");

	public static void register() {
		RenderedArmorInfo.register();

		if(ACCESSORIES_PRESENT) {
			CharaFormAct.LOGGER.info("Accessories is loaded! Establishing compatibility...");
			AccessoriesCompatUnsafe.register();
		}
	}

	public static <T> T getWrappedAccessoryRenderer(T original) {
		return AccessoriesCompatUnsafe.wrap(original);
	}
}
