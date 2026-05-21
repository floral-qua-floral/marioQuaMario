package com.fqf.mario_qua_mario;

import com.fqf.charaformact_api.appearance.ClientAppearanceDefinition;
import com.fqf.mario_qua_mario.entity.ModEntities;
import com.fqf.mario_qua_mario.entity.MarioFireballModel;
import com.fqf.mario_qua_mario.entity.MarioFireballRenderer;
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

public class MarioQuaMarioClient implements ClientModInitializer {
	public static final Identifier COIN_GROUND_ID = MarioQuaMario.makeResID("coin_ground").withPrefixedPath("item/");
	public static final ModelIdentifier COIN_GROUND_MODEL_ID = new ModelIdentifier(COIN_GROUND_ID, "fabric_resource");

	@Override
	public void onInitializeClient() {
		MarioQuaMario.LOGGER.info("Mario qua Mario initializing on the client...");

		MarioQuaMario.clientHelper = new ContentClientHelperImplementation();

		EntityModelLayerRegistry.registerModelLayer(MarioFireballModel.FIREBALL, MarioFireballModel::getTexturedModelData);
		EntityRendererRegistry.register(ModEntities.MARIO_FIREBALL, MarioFireballRenderer::new);

		ModelLoadingPlugin.register(context -> {
			context.addModels(COIN_GROUND_ID);
		});

	}

	public static class ContentClientHelperImplementation extends MarioQuaMario.ContentClientHelper {
		@Override
		public MutableText getBackflipDismountText() {
			GameOptions options = MinecraftClient.getInstance().options;
			return Text.translatable("mount.onboard.mario", options.sneakKey.getBoundKeyLocalizedText(), options.jumpKey.getBoundKeyLocalizedText());
		}
	}

	public static Identifier makeAppearanceTextureID(ClientAppearanceDefinition definition) {
		return MarioQuaMario.makeID("textures/entity/player/appearance/" + definition.getCharacterID().getPath()
				+ "/" + definition.getFormID().getPath() + ".png");
	}
}