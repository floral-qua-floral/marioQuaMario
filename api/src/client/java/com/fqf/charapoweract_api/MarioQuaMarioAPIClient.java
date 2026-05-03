package com.fqf.charapoweract_api;

import net.fabricmc.api.ClientModInitializer;

public class MarioQuaMarioAPIClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MarioQuaMarioAPI.LOGGER.info("Mario qua Mario API Client initializing...");
	}
}