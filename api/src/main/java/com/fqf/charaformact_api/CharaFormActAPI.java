package com.fqf.charaformact_api;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharaFormActAPI implements ModInitializer {
	public static final String MOD_ID = "charaformact_api";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("CharaFormAct API initializing!");
	}
}