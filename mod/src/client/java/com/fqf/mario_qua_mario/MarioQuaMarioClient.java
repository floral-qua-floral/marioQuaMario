package com.fqf.mario_qua_mario;

import net.fabricmc.api.ClientModInitializer;

public class MarioQuaMarioClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MarioQuaMario.LOGGER.info("Mario qua Mario Client initializing...");
	}
}