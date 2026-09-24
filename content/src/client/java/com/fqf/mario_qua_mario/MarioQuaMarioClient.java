package com.fqf.mario_qua_mario;

import com.fqf.mario_qua_mario.entity.MQMEntities;
import com.fqf.mario_qua_mario.entity.MarioFireballModel;
import com.fqf.mario_qua_mario.entity.MarioFireballRenderer;
import com.fqf.mario_qua_mario.freezing.EncasementRenderUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.FlyingItemEntityRenderer;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.util.Identifier;

public class MarioQuaMarioClient implements ClientModInitializer {
	public static final Identifier COIN_GROUND_ID = MarioQuaMario.makeResID("coin_ground").withPrefixedPath("item/");
	public static final ModelIdentifier COIN_GROUND_MODEL_ID = new ModelIdentifier(COIN_GROUND_ID, "fabric_resource");

	@Override
	public void onInitializeClient() {
		MarioQuaMario.LOGGER.info("Mario qua Mario initializing on the client...");

		EntityModelLayerRegistry.registerModelLayer(MarioFireballModel.FIREBALL, MarioFireballModel::getTexturedModelData);
		EntityRendererRegistry.register(MQMEntities.MARIO_FIREBALL, MarioFireballRenderer::new);
		EntityRendererRegistry.register(MQMEntities.MARIO_ICEBALL, FlyingItemEntityRenderer::new);

		ModelLoadingPlugin.register(context -> {
			context.addModels(COIN_GROUND_ID);
		});

		ClientTickEvents.END_WORLD_TICK.register(world -> {
//			if(world.getTime() % 1200 == 0) // Once every minute, assuming normal tickrate.
				EncasementRenderUtil.clearCachedCuboids();
		});
	}
}