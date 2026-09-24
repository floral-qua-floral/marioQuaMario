package com.fqf.mario_qua_mario.freezing;

import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import it.unimi.dsi.fastutil.doubles.DoubleDoublePair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profiler;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class EncasementRenderUtil {
	private static final RenderLayer ICE_LAYER =
			RenderLayer.getEntityTranslucentCull(Identifier.ofVanilla("textures/block/ice.png"));
	private static final Set<Direction> ALL_DIRECTIONS = EnumSet.allOf(Direction.class);

	private static final Map<DoubleDoublePair, ModelPart.Cuboid> CACHED_ICE_CUBOIDS =
			new HashMap<>();

	public static void renderEncasingIce(
			Entity entity, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light
	) {
		Profiler profiler = MinecraftClient.getInstance().getProfiler();
		profiler.push("encasingIce");

		ModelPart.Cuboid cuboid = CACHED_ICE_CUBOIDS.computeIfAbsent(
				new DoubleDoubleImmutablePair(entity.getWidth(), entity.getHeight()),
				pair -> makeCuboid(entity)
		);
		cuboid.renderCuboid(matrices.peek(), vertexConsumers.getBuffer(ICE_LAYER), light, OverlayTexture.DEFAULT_UV, Colors.WHITE);

		float miningProgress = ((BreakableEntity) entity).mqm$getMiningProgress();
		if(miningProgress != 0) {
			int texture = Math.min(9, MathHelper.floor(miningProgress * 9));
			cuboid.renderCuboid(
					matrices.peek(),
					vertexConsumers.getBuffer(ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.get(texture)),
					light,
					OverlayTexture.DEFAULT_UV,
					Colors.WHITE
			);
		}

		MinecraftClient client = MinecraftClient.getInstance();
		if(
				!client.options.hudHidden
				&& client.crosshairTarget instanceof EntityHitResult crosshairTarget
				&& entity.equals(crosshairTarget.getEntity())
		) {
			WorldRenderer.drawBox(
					matrices,
					vertexConsumers.getBuffer(RenderLayer.getLines()),
					entity.getBoundingBox().offset(-entity.getX(), -entity.getY(), -entity.getZ()),
					0,
					0,
					0,
					0.4F
			);
		}

		profiler.pop();
	}

	private static ModelPart.Cuboid makeCuboid(Entity entity) {
		Box iceBox = entity.getBoundingBox().offset(entity.getPos().multiply(-1));

		return new ModelPart.Cuboid(
				0, 0,
				(float) iceBox.minX * 16, (float) iceBox.minY * 16, (float) iceBox.minZ * 16,
				(float) iceBox.getLengthX() * 16, (float) iceBox.getLengthY() * 16, (float) iceBox.getLengthZ() * 16,
				0, 0, 0,
				false, 16, 16,
				ALL_DIRECTIONS
		);
	}

	// Clearing the cache is a kind of stupid way to prevent memory leaks, but it should work.
	public static void clearCachedCuboids() {
		CACHED_ICE_CUBOIDS.clear();
	}
}
