package com.floralquafloral;

import com.floralquafloral.mariodata.MarioDataManager;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarioQuaMario implements ModInitializer {
	public static final String MOD_ID = "qua_mario";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("MarioQuaMario.java loaded on environment type " + FabricLoader.getInstance().getEnvironmentType());
		MarioDataManager.wipePlayerData();
		MarioPackets.registerCommon();
	}
}