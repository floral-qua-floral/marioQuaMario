package com.fqf.charaformact;

import com.fqf.charaformact.models.PlayerModelCollector;
import com.fqf.charaformact.packets.CfaClientPacketHelper;
import com.fqf.charaformact.util.CfaClientEventListeners;
import com.fqf.charaformact.util.CfaClientHelperManager;
import net.fabricmc.api.ClientModInitializer;

public class CharaFormActClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CharaFormAct.LOGGER.info("CharaFormAct initializing on the client...");

		CfaClientHelperManager.helper = new CfaClientHelper();
		CfaClientHelperManager.packetSender = new CfaClientPacketHelper();

		CfaClientPacketHelper.registerClientReceivers();

		CfaClientEventListeners.register();

		PlayerModelCollector.parseModelDefinitions();
	}
}