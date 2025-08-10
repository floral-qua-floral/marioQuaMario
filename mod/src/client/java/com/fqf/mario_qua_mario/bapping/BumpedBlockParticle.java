package com.fqf.mario_qua_mario.bapping;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static net.minecraft.client.render.RenderPhase.*;

public class BumpedBlockParticle extends Particle {
	private final BlockPos POSITION;
	public final Direction DIRECTION;
	public final Vector3f BUMP_UNIT_VECTOR;
	private final BlockRenderManager RENDERER;

	private BlockState blockState;
	public boolean replaced = false;

	protected BumpedBlockParticle(ClientWorld world, BlockPos blockPos, Direction direction, boolean cutShort) {
		super(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());

		this.POSITION = blockPos;
		this.DIRECTION = direction;
		this.BUMP_UNIT_VECTOR = direction.getUnitVector();
		this.RENDERER = MinecraftClient.getInstance().getBlockRenderManager();

		this.blockState = world.getBlockState(blockPos);

		this.gravityStrength = 0.0F;
		this.maxAge = (BumpingBlockInfo.BUMP_DURATION + 1) / (cutShort ? 2 : 1);
	}

	@Override
	public void tick() {
		this.blockState = world.getBlockState(this.POSITION);
		if(this.blockState.isAir()) this.markDead();

		super.tick();
	}

	@Override
	public void markDead() {
		BlockBappingClientUtil.PARTICLES.get(this.world).remove(this.POSITION, this);
		super.markDead();
	}

	private static final float SCALE = 1.005F;
	private static final float MINI_OFFSET = (1.0F - SCALE) / 2.0F;

	private static final float UNSCALE = 1 + (1.0F - SCALE);

	@Override
	public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		if(this.blockState.getRenderType() != BlockRenderType.MODEL || this.dead) return;

		MatrixStack matrixStack = new MatrixStack();
		matrixStack.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z); // <- probably dumb??

		this.applyOffset(matrixStack, tickDelta);

		// Slightly change scale to prevent Z-fighting
		matrixStack.translate(MINI_OFFSET, MINI_OFFSET, MINI_OFFSET);
		matrixStack.scale(SCALE, SCALE, SCALE);

		VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();

//		BufferBuilderStorage bBStorage = MinecraftClient.getInstance().getBufferBuilders();
//		VertexConsumer vConsumerCrumbleEffect = bBStorage.getEffectVertexConsumers().getBuffer(ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.get(8));
//		VertexConsumer vConsumerCrumbleEntity = bBStorage.getEntityVertexConsumers().getBuffer(ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.get(8));
//		MatrixStack.Entry entry = matrixStack.peek();
//		VertexConsumer overlayVConsumerCrumbleEffect = new OverlayVertexConsumer(vConsumerCrumbleEffect, entry, 1.0F);
//		VertexConsumer overlayVConsumerCrumbleEntity = new OverlayVertexConsumer(vConsumerCrumbleEntity, entry, 1.0F);


		this.RENDERER.getModelRenderer().render(
				world,
				this.RENDERER.getModel(this.blockState),
				this.blockState,
				this.POSITION,
				matrixStack,
				immediate.getBuffer(RenderLayers.getMovingBlockLayer(this.blockState)),
//				VertexConsumers.union(immediate.getBuffer(MOVING_CRUMBLING), immediate.getBuffer(RenderLayers.getMovingBlockLayer(this.blockState))),
				false,
				Random.create(),
				this.blockState.getRenderingSeed(this.POSITION),
				OverlayTexture.DEFAULT_UV
		);

		// TODO: figure out how to render cracks on the bumped block? some day???? :/

//		matrixStack.scale(UNSCALE, UNSCALE, UNSCALE);
//		matrixStack.translate(-MINI_OFFSET, -MINI_OFFSET, -MINI_OFFSET);
//
//		matrixStack.translate(-0.5, -0.5, -0.5);
//		matrixStack.scale(2, 2, 2);

//		VertexConsumer vertexConsumer2 = new OverlayVertexConsumer(
//				MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers().getBuffer(ModelLoader.BLOCK_DESTRUCTION_RENDER_LAYERS.get(9)), matrixStack.peek(), 1.0F
//		);
//		this.RENDERER.renderDamage(this.blockState, this.POSITION, this.world, matrixStack, vertexConsumer2);

//		this.RENDERER.getModelRenderer().render(
//				world,
//				this.RENDERER.getModel(this.blockState),
//				this.blockState,
//				this.POSITION,
//				matrixStack,
//				immediate.getBuffer(MOVING_CRUMBLING),
//				false,
//				Random.create(),
//				this.blockState.getRenderingSeed(this.POSITION),
//				OverlayTexture.DEFAULT_UV
//		);
	}

//	private static final Texture CRUMBLING_TEXTURE = new RenderPhase.Texture(ModelLoader.BLOCK_DESTRUCTION_STAGE_TEXTURES.get(8), false, false);
//	private static final RenderLayer MOVING_CRUMBLING = RenderLayer.of(
//			"crumbling", VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL, VertexFormat.DrawMode.QUADS, 1536, false, true,
//			RenderLayer.MultiPhaseParameters.builder()
//					.program(CRUMBLING_PROGRAM)
//					.texture(CRUMBLING_TEXTURE)
//					.transparency(CRUMBLING_TRANSPARENCY)
//					.lightmap(ENABLE_LIGHTMAP)
//					.target(ITEM_ENTITY_TARGET)
//					.build(false)
//	);

	public float calculateOffset(float tickDelta) {
		float progress = Math.min(1, (tickDelta + this.age) / BumpingBlockInfo.BUMP_DURATION);
		return 0.5F * (1 - (2 * ((progress - 1) * (progress - 1)) - 1) * (2 * ((progress - 1) * (progress - 1)) - 1));
	}

	public Vec3d applyOffset(Vec3d vector, float tickDelta) {
		return vector.offset(this.DIRECTION, this.calculateOffset(tickDelta));
	}

	public void applyOffset(MatrixStack matrices, float tickDelta) {
		float offset = this.calculateOffset(tickDelta);
		matrices.translate(offset * this.BUMP_UNIT_VECTOR.x, offset * this.BUMP_UNIT_VECTOR.y, offset * this.BUMP_UNIT_VECTOR.z);
//		this.lastOffset = offset;
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.CUSTOM;
	}

	public static class Factory implements ParticleFactory<SimpleParticleType> {
		@Override
		public @Nullable Particle createParticle(SimpleParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
			BlockPos position = BlockPos.ofFloored(x, y, z);
			return new BumpedBlockParticle(world, position, Direction.UP, false);
		}
	}
}
