package com.fqf.charaformact;

import com.fqf.charaformact.models.CharacterFormEntityModel;
import com.fqf.charaformact.models.CfaPlayerModelHelper;
import com.fqf.charaformact.packets.CfaClientPacketHelper;
import com.fqf.charaformact.util.CfaClientEventListeners;
import com.fqf.charaformact.util.CfaClientHelperManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.render.entity.model.EntityModelLayer;

public class CharaFormActClient implements ClientModInitializer {
	public static final EntityModelLayer TEST_LAYER = new EntityModelLayer(CharaFormAct.makeID("test_player_model"), "main");

	@Override
	public void onInitializeClient() {
		CharaFormAct.LOGGER.info("CharaFormAct initializing on the client...");

		CfaClientHelperManager.helper = new CfaClientHelper();
		CfaClientHelperManager.packetSender = new CfaClientPacketHelper();

		CfaClientPacketHelper.registerClientReceivers();

		CfaClientEventListeners.register();

		CfaPlayerModelHelper.registerCharacterFormCombos();

		EntityModelLayerRegistry.registerModelLayer(TEST_LAYER, CharacterFormEntityModel::getTexturedModelData);
	}
}