package com.fqf.mario_qua_mario_content;

import com.fqf.mario_qua_mario_content.entity.ModEntities;
import com.fqf.mario_qua_mario_content.entity.MarioFireballModel;
import com.fqf.mario_qua_mario_content.entity.MarioFireballRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class MarioQuaMarioContentClient implements ClientModInitializer {
	public static final Identifier COIN_GROUND_ID = MarioQuaMarioContent.makeResID("coin_ground").withPrefixedPath("item/");
	public static final ModelIdentifier COIN_GROUND_MODEL_ID = new ModelIdentifier(COIN_GROUND_ID, "fabric_resource");

	@Override
	public void onInitializeClient() {
		MarioQuaMarioContent.LOGGER.info("Mario qua Mario Content Client initializing...");

		MarioQuaMarioContent.clientHelper = new ContentClientHelperImplementation();

		EntityModelLayerRegistry.registerModelLayer(MarioFireballModel.FIREBALL, MarioFireballModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.MARIO_FIREBALL, MarioFireballRenderer::new);

		ModelLoadingPlugin.register(context -> {
			context.addModels(COIN_GROUND_ID);
		});

	}

	public static class ContentClientHelperImplementation extends MarioQuaMarioContent.ContentClientHelper {
		@Override
		public MutableText getBackflipDismountText() {
			GameOptions options = MinecraftClient.getInstance().options;
			return Text.translatable("mount.onboard.mario", options.sneakKey.getBoundKeyLocalizedText(), options.jumpKey.getBoundKeyLocalizedText());
		}
	}
}