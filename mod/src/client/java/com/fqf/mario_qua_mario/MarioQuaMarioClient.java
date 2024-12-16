package com.fqf.mario_qua_mario;

import net.fabricmc.api.ClientModInitializer;

public class MarioQuaMarioClient implements ClientModInitializer {
	// This is in the client sources

	@Override
	public void onInitializeClient() {
		MarioQuaMario.LOGGER.info("Mario qua Mario Client initializing...");

		MarioAbstractClientHelper.instance = new MarioClientHelper();
	}
}