package com.fqf.charaformact.util;

import com.fqf.charaformact.bapping.BlockBappingClientUtil;
import com.fqf.charaformact.bapping.BlockBappingUtil;
import com.fqf.charaformact.bapping.WorldBapsInfo;
import com.fqf.charaformact.compat.optional.SableCompatSafe;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Quaternionf;
import org.joml.Vector3dc;

public class CfaClientEventListeners {
	public static void register() {
		ClientTickEvents.START_WORLD_TICK.register(BlockBappingClientUtil::clientWorldTick);

		ClientEntityEvents.ENTITY_UNLOAD.register((entity, world) -> {
			if(entity instanceof PlayerEntity player) player.cfa$getCfaData().removeCharacterFormAttributeModifiers();
		});

		WorldRenderEvents.AFTER_ENTITIES.register((worldRenderContext) -> {
			ClientWorld world = worldRenderContext.world();
			WorldBapsInfo worldBaps = BlockBappingUtil.getBapsInfoNullable(world);
			if(worldBaps == null) return;
			MatrixStack matrixStack = worldRenderContext.matrixStack();
			assert matrixStack != null;
			Vec3d cameraPos = worldRenderContext.camera().getPos();

			for(BlockPos pos : worldBaps.HIDDEN) {
				BlockBappingClientUtil.renderBumpedBlock(world, worldBaps, matrixStack, cameraPos, pos, worldBaps.BRITTLE.contains(pos));
			}

			worldBaps.HIDDEN_LINGERING.removeIf(lingerer -> {
				boolean isFinished = lingerer.framesRemaining-- <= 0;
				if(!isFinished)
					BlockBappingClientUtil.renderBumpedBlock(world, worldBaps, matrixStack, cameraPos, lingerer.POS, lingerer.IS_BRITTLE);
				return isFinished;
			});

			for(BlockPos pos : worldBaps.BRITTLE) {
				if(worldBaps.HIDDEN.contains(pos) || worldBaps.HIDDEN_LINGERING.stream().anyMatch(lingerer -> lingerer.POS.equals(pos))) {
					continue;
				}

				Triple<Vec3d, Vector3dc, Quaternionf> brittleBlockPosAndOrientation = SableCompatSafe.getPosAndOrientation(world, pos);
				Vec3d brittleBlockPos = brittleBlockPosAndOrientation.getLeft();
				Vector3dc brittleBlockScale = brittleBlockPosAndOrientation.getMiddle();
				Quaternionf brittleBlockOrientation = brittleBlockPosAndOrientation.getRight();

				double diffX = brittleBlockPos.x - cameraPos.x;
				double diffY = brittleBlockPos.y - cameraPos.y;
				double diffZ = brittleBlockPos.z - cameraPos.z;

				if(diffX * diffX + diffY * diffY + diffZ * diffZ < 1024) {
					matrixStack.push();
					matrixStack.translate(diffX, diffY, diffZ);
					matrixStack.scale((float) brittleBlockScale.x(), (float) brittleBlockScale.y(), (float) brittleBlockScale.z());
					matrixStack.multiply(brittleBlockOrientation);
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
		});
	}
}
