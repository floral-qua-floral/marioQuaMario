package com.fqf.mario_qua_mario.compat.optional;

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
