package com.fqf.charapoweract.compat.optional;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;

public class MarioSableCompatSafe {
	public static final boolean SABLE_PRESENT = FabricLoader.getInstance().isModLoaded("sable");

	public static void trySablePostTravelCompatibility(PlayerEntity mario) {
		if(SABLE_PRESENT) {
			MarioSableCompatUnsafe.runPostTravelSableMethods(mario);
		}
	}
}
