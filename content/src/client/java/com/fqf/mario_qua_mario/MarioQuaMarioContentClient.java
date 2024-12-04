package com.fqf.mario_qua_mario;

import net.fabricmc.api.ClientModInitializer;

public class MarioQuaMarioContentClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MarioQuaMarioContent.LOGGER.info("Mario qua Mario Content Client initializing...");
	}
}