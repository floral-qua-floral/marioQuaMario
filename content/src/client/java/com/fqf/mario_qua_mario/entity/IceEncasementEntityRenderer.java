package com.fqf.mario_qua_mario.entity;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.entity.custom.IceEncasementEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.EnumSet;
import java.util.Set;

public class IceEncasementEntityRenderer extends EntityRenderer<IceEncasementEntity> {
	public static final Identifier ICE_TEXTURE = Identifier.ofVanilla("textures/block/ice.png");
	private static final Set<Direction> ALL_DIRECTIONS = EnumSet.allOf(Direction.class);

	public IceEncasementEntityRenderer(EntityRendererFactory.Context ctx) {
		super(ctx);
	}

	@Override
	public void render(IceEncasementEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light);

		if(entity.isShaking()) {
			matrices.translate(
					entity.getRandom().nextTriangular(0, 0.05),
					entity.isOnGround() ? 0 : entity.getRandom().nextTriangular(0, 0.05),
					entity.getRandom().nextTriangular(0, 0.05)
			);
		}

		MinecraftClient client = MinecraftClient.getInstance();
		Entity encased = entity.getOwner();
		if(encased != null) {
			client.getEntityRenderDispatcher().render(
					encased,
					0, 0, 0,
					encased.getYaw(),
					0,
					matrices,
					vertexConsumers,
					light
			);
		}

		if(entity.cuboidForClientRendering == null) {
			Box iceBox = entity.getBoundingBox().offset(entity.getPos().multiply(-1));

			entity.cuboidForClientRendering = new ModelPart.Cuboid(
					0, 0,
					(float) iceBox.minX * 16, (float) iceBox.minY * 16, (float) iceBox.minZ * 16,
					(float) iceBox.getLengthX() * 16, (float) iceBox.getLengthY() * 16, (float) iceBox.getLengthZ() * 16,
					0, 0, 0,
					false, 16, 16,
					ALL_DIRECTIONS
			);
		}

		ModelPart.Cuboid cuboid = ((ModelPart.Cuboid) entity.cuboidForClientRendering);
		cuboid.renderCuboid(
				matrices.peek(),
				vertexConsumers.getBuffer(RenderLayer.getEntityTranslucentCull(this.getTexture(entity))),
				light,
				OverlayTexture.DEFAULT_UV,
				Colors.WHITE
		);

		float miningProgress = entity.getMiningProgressPercentage();
		if(miningProgress != 0) {
			int texture = MathHelper.clamp(MathHelper.floor(miningProgress * 10), 0, 9);
			cuboid.renderCuboid(
					matrices.peek(),
					vertexConsumers.getBuffer(ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.get(texture)),
					light,
					OverlayTexture.DEFAULT_UV,
					Colors.WHITE
			);
		}

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
	}

	@Override
	public Identifier getTexture(IceEncasementEntity entity) {
		return ICE_TEXTURE;
	}
}
