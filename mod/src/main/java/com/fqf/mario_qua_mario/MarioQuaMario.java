package com.fqf.mario_qua_mario;

import com.fqf.mario_qua_mario.registries.RegistryManager;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarioQuaMario implements ModInitializer {
	public static final String MOD_ID = "mario_qua_mario";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Mario qua Mario initializing...");

		RegistryManager.registerAll();

		MarioCommand.registerMarioCommand();
	}

	public static Identifier makeID(String path) {
		return Identifier.of("mqm", path);
	}
}