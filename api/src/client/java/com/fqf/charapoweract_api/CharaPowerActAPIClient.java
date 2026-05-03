package com.fqf.charapoweract_api;

import net.fabricmc.api.ClientModInitializer;

public class CharaPowerActAPIClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CharaPowerActAPI.LOGGER.info("CharaPowerAct API Client initializing...");
	}
}