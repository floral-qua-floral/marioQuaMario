package com.fqf.mario_qua_mario;

import com.fqf.mario_qua_mario.packets.MarioClientPacketHelper;
import net.fabricmc.api.ClientModInitializer;

public class MarioQuaMarioClient implements ClientModInitializer {
	// This is in the client sources

	@Override
	public void onInitializeClient() {
		MarioQuaMario.LOGGER.info("Mario qua Mario Client initializing...");

		MarioClientHelperManager.helper = new MarioClientHelper();
		MarioClientHelperManager.packetSender = new MarioClientPacketHelper();

		MarioClientPacketHelper.registerClientReceivers();
	}
}