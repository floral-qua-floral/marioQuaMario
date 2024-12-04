package com.floralquafloral;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarioQuaMarioDataGenerator implements DataGeneratorEntrypoint {
	public static final String MOD_ID = "qua_mario_datagen";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		LOGGER.info("Hewwo?");
	}
}
