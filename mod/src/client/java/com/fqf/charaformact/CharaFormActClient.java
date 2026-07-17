package com.fqf.charaformact;

import com.fqf.charaformact.appearance.ClientAppearanceCollector;
import com.fqf.charaformact.compat.EquipmentSlotModsCompatSafe;
import com.fqf.charaformact.packets.CfaClientPacketHelper;
import com.fqf.charaformact.registries.actions.parsed.ParsedMountedAction;
import com.fqf.charaformact.util.CfaClientEventListeners;
import com.fqf.charaformact.util.CfaClientHelperManager;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;

public class CharaFormActClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		CharaFormAct.LOGGER.info("CharaFormAct initializing on the client...");

		CfaClientHelperManager.helper = new CfaClientHelper();
		CfaClientHelperManager.packetSender = new CfaClientPacketHelper();

		CfaClientPacketHelper.registerClientReceivers();

		CfaClientEventListeners.register();

		ClientAppearanceCollector.INSTANCE.collect();

		CharaFormAct.clientHelper = new ClientHelperImpl();

		EquipmentSlotModsCompatSafe.register();
	}

	private static class ClientHelperImpl extends CharaFormAct.ClientHelper {
		@Override
		public void prepareKeybindTexts() {
			GameOptions options = MinecraftClient.getInstance().options;
			ParsedMountedAction.sneakKeybind = options.sneakKey.getBoundKeyLocalizedText();
			ParsedMountedAction.jumpKeybind = options.jumpKey.getBoundKeyLocalizedText();
			ParsedMountedAction.attackKeybind = options.attackKey.getBoundKeyLocalizedText();
			ParsedMountedAction.forwardKeybind = options.forwardKey.getBoundKeyLocalizedText();
			ParsedMountedAction.backwardKeybind = options.backKey.getBoundKeyLocalizedText();
			ParsedMountedAction.vanillaHint = Text.translatable("mount.onboard", options.sneakKey.getBoundKeyLocalizedText());
		}
	}
}