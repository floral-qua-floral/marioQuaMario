package com.fqf.charaformact_api;

import net.fabricmc.api.ClientModInitializer;

public class CharaFormActAPIClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CharaFormActAPI.LOGGER.info("CharaFormAct API initializing on the client...");
	}
}