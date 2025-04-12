package com.fqf.mario_qua_mario_content;

import com.fqf.mario_qua_mario_content.entity.ModEntities;
import com.fqf.mario_qua_mario_content.entity.MarioFireballModel;
import com.fqf.mario_qua_mario_content.entity.MarioFireballRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;

public class MarioQuaMarioContentClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		MarioQuaMarioContent.LOGGER.info("Mario qua Mario Content Client initializing...");

		EntityModelLayerRegistry.registerModelLayer(MarioFireballModel.FIREBALL, MarioFireballModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.MARIO_FIREBALL, MarioFireballRenderer::new);
	}

	public static class ContentClientHelperImplemented extends MarioQuaMarioContent.ContentClientHelper {
		@Override
		public Text getBackflipDismountText() {
			GameOptions options = MinecraftClient.getInstance().options;
			return Text.translatable("mount.onboard.mario", options.sneakKey.getBoundKeyLocalizedText(), options.jumpKey.getBoundKeyLocalizedText());
		}
	}
}