package com.fqf.mario_qua_mario;

import com.fqf.mario_qua_mario.util.MarioContentEventListeners;
import com.fqf.mario_qua_mario.util.MarioContentGamerules;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarioQuaMarioContent implements ModInitializer {
	public static final String MOD_ID = "mario_qua_mario_content";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Mario qua Mario Content intializing...");

		MarioContentSFX.staticInitialize();
		MarioContentGamerules.register();
		MarioContentEventListeners.register();
	}

	public static Identifier makeID(String path) {
		return Identifier.of("mqm", path);
	}
	public static Identifier makeResID(String path) {
		return Identifier.of("mario_qua_mario", path);
	}
}