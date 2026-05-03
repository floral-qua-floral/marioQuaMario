package com.fqf.charapoweract_api;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CharaPowerActAPI implements ModInitializer {
	public static final String MOD_ID = "mario_qua_mario_api";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("CharaPowerAct API initializing!");
	}
}