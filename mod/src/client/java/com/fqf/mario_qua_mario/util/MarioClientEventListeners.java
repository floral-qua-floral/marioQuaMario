package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.bapping.BlockBappingClientUtil;
import com.fqf.mario_qua_mario.bapping.BlockBappingUtil;
import com.fqf.mario_qua_mario.bapping.WorldBapsInfo;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

import java.util.Set;

public class MarioClientEventListeners {
	public static void register() {
		ClientTickEvents.START_WORLD_TICK.register(BlockBappingClientUtil::clientWorldTick);

		WorldRenderEvents.AFTER_ENTITIES.register((worldRenderContext) -> {
			ClientWorld world = worldRenderContext.world();
			WorldBapsInfo worldBaps = BlockBappingUtil.getBapsInfoNullable(world);
			if(worldBaps == null) return;
			MatrixStack matrixStack = worldRenderContext.matrixStack();
			assert matrixStack != null;
			Vec3d cameraPos = worldRenderContext.camera().getPos();

			for(BlockPos pos : worldBaps.BRITTLE) {
				if(worldBaps.HIDDEN.contains(pos)) {
					continue;
				}

				double diffX = (double)pos.getX() - cameraPos.x;
				double diffY = (double)pos.getY() - cameraPos.y;
				double diffZ = (double)pos.getZ() - cameraPos.z;

				if(diffX * diffX + diffY * diffY + diffZ * diffZ < 1024) {
					matrixStack.push();
					matrixStack.translate(diffX, diffY, diffZ);
					MatrixStack.Entry entry3 = matrixStack.peek();

					VertexConsumer vertexConsumer2 = new OverlayVertexConsumer(
							MinecraftClient.getInstance().getBufferBuilders().getEffectVertexConsumers().getBuffer(ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.get(9)), entry3, 1.0F
					);
					MinecraftClient.getInstance().getBlockRenderManager().renderDamage(
							world.getBlockState(pos),
							pos,
							world,
							matrixStack,
							vertexConsumer2
					);
					matrixStack.pop();
				}
			}

//			return true;
		});


	}
}
