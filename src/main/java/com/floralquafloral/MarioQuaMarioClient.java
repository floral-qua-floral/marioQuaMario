package com.floralquafloral;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class MarioQuaMarioClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MarioQuaMario.LOGGER.info("MarioQuaMarioClient.java loaded on " + FabricLoader.getInstance().getEnvironmentType());
		MarioPackets.registerClient();
	}
}
