package com.floralquafloral;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.SimpleOption;

public class MarioQuaMarioClient implements ClientModInitializer {
	public static boolean useCharacterStats = true;

	public static final SimpleOption<Boolean> ALWAYS_FALSE = SimpleOption.ofBoolean("alwaysFalseOption", false);

	@Override
	public void onInitializeClient() {
		MarioQuaMario.LOGGER.info("MarioQuaMarioClient.java loaded on {}", FabricLoader.getInstance().getEnvironmentType());
		MarioPackets.registerClient();
	}
}
