package com.fqf.mario_qua_mario;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarioQuaMarioContent implements ModInitializer {
	public static final String MOD_ID = "mario_qua_mario_content";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Mario qua Mario Content intializing...");
	}
}