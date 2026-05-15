package com.fqf.charaformact;

import com.fqf.charaformact.models.GradientPlayermodel;
import com.fqf.charaformact.models.TemplatePlayermodel;
import com.fqf.charaformact.models.CfaPlayerModelHelper;
import com.fqf.charaformact.packets.CfaClientPacketHelper;
import com.fqf.charaformact.util.CfaClientEventListeners;
import com.fqf.charaformact.util.CfaClientHelperManager;
import com.fqf.charaformact_api.model.CharacterFormModelDefinition;
import com.fqf.charaformact_api.model.CharacterFormModelHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

public class CharaFormActClient implements ClientModInitializer {
	public static CharacterFormModelDefinition TEST_MODEL = new TemplatePlayermodel();
	public static CharacterFormModelDefinition GRADIENT_MODEL = new GradientPlayermodel();

	@Override
	public void onInitializeClient() {
		CharaFormAct.LOGGER.info("CharaFormAct initializing on the client...");

		CfaClientHelperManager.helper = new CfaClientHelper();
		CfaClientHelperManager.packetSender = new CfaClientPacketHelper();

		CfaClientPacketHelper.registerClientReceivers();

		CfaClientEventListeners.register();

		CfaPlayerModelHelper.registerCharacterFormCombos();

		EntityModelLayerRegistry.registerModelLayer(TEST_MODEL.getModelLayer(), () ->
				TEST_MODEL.getTexturedModelData(new CharacterFormModelHelper() { }));
		EntityModelLayerRegistry.registerModelLayer(GRADIENT_MODEL.getModelLayer(), () ->
				GRADIENT_MODEL.getTexturedModelData(new CharacterFormModelHelper() { }));
	}
}