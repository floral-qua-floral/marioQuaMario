package com.fqf.mario_qua_mario_api;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarioQuaMarioAPI implements ModInitializer {
	public static final String MOD_ID = "mario_qua_mario_api";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Mario qua Mario API initializing!");
	}
}