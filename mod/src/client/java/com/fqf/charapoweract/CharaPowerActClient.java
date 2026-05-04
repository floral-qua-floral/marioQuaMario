package com.fqf.charapoweract;

import com.fqf.charapoweract.packets.CPAClientPacketHelper;
import com.fqf.charapoweract.util.CharaPowerActClientEventListeners;
import com.fqf.charapoweract.util.CPAClientHelperManager;
import net.fabricmc.api.ClientModInitializer;

public class CharaPowerActClient implements ClientModInitializer {
	// This is in the client sources

	@Override
	public void onInitializeClient() {
		CharaPowerAct.LOGGER.info("CharaPowerAct initializing on the client...");

		CPAClientHelperManager.helper = new CPAClientHelper();
		CPAClientHelperManager.packetSender = new CPAClientPacketHelper();

		CPAClientPacketHelper.registerClientReceivers();

		CharaPowerActClientEventListeners.register();
	}
}