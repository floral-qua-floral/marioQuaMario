package com.floralquafloral.bumping;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

// copied in large part from https://github.com/enjarai/a-good-place/blob/master/common/src/main/java/nl/enjarai/a_good_place/particles/PlacingBlockParticle.java
public class BumpedBlockParticle extends Particle {
	private final BlockPos POSITION;
	public final Vector3f BUMP_UNIT_VECTOR;
	private final BlockRenderManager RENDERER;

	private BlockState blockState;
	private int lightLevel;
	public boolean replaced = false;

	public float lastOffset;

	private final RenderLayer LAYER = RenderLayer.getTranslucentMovingBlock();

	protected BumpedBlockParticle(ClientWorld world, BlockPos blockPos, BlockState blockState, Direction direction) {
		super(world, blockPos.getX(), blockPos.getY(), blockPos.getZ());

		this.POSITION = blockPos;
		this.BUMP_UNIT_VECTOR = direction.getUnitVector();
		this.RENDERER = MinecraftClient.getInstance().getBlockRenderManager();

		this.blockState = blockState;
//		this.lightLevel = getNearbyLightLevel();

		this.gravityStrength = 0.0F;
		this.maxAge = 7;
	}

	@Override
	public void tick() {
//		this.lightLevel = getNearbyLightLevel();
		this.blockState = world.getBlockState(this.POSITION);
		if(this.blockState.isAir()) this.markDead();

		super.tick();

		// End the bump 1 tick early to prevent flickering
		if(this.age >= this.maxAge - 1) BumpManagerClient.endBump(this, this.world, this.POSITION, this.blockState);
	}

	@Override
	public void markDead() {
		if(!replaced) BumpManagerClient.endBump(this, this.world, this.POSITION, this.blockState);
		super.markDead();
	}

	private int getNearbyLightLevel() {
		int lightHere = this.world.getLightLevel(this.POSITION);
		if(lightHere != 0) return WorldRenderer.getLightmapCoordinates(this.world, this.POSITION);

		int mostIlluminatedLightLevel = -1;
		BlockPos mostIlluminatedLightPosition = this.POSITION;

		BlockPos comparePos;
		int compareLevel;

		// wow. this is god awful
		comparePos = this.POSITION.up(); compareLevel = this.world.getLightLevel(comparePos);
		if(compareLevel > mostIlluminatedLightLevel) {
			mostIlluminatedLightLevel = compareLevel;
			mostIlluminatedLightPosition = comparePos;
		}
		comparePos = this.POSITION.down(); compareLevel = this.world.getLightLevel(comparePos);
		if(compareLevel > mostIlluminatedLightLevel) {
			mostIlluminatedLightLevel = compareLevel;
			mostIlluminatedLightPosition = comparePos;
		}
		comparePos = this.POSITION.north(); compareLevel = this.world.getLightLevel(comparePos);
		if(compareLevel > mostIlluminatedLightLevel) {
			mostIlluminatedLightLevel = compareLevel;
			mostIlluminatedLightPosition = comparePos;
		}
		comparePos = this.POSITION.south(); compareLevel = this.world.getLightLevel(comparePos);
		if(compareLevel > mostIlluminatedLightLevel) {
			mostIlluminatedLightLevel = compareLevel;
			mostIlluminatedLightPosition = comparePos;
		}
		comparePos = this.POSITION.east(); compareLevel = this.world.getLightLevel(comparePos);
		if(compareLevel > mostIlluminatedLightLevel) {
			mostIlluminatedLightLevel = compareLevel;
			mostIlluminatedLightPosition = comparePos;
		}
		comparePos = this.POSITION.west(); compareLevel = this.world.getLightLevel(comparePos);
		if(compareLevel > mostIlluminatedLightLevel) {
			mostIlluminatedLightPosition = comparePos;
		}

		return WorldRenderer.getLightmapCoordinates(this.world, mostIlluminatedLightPosition);
	}

	@Override
	public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
		if(this.blockState.getRenderType() != BlockRenderType.MODEL || this.dead) return;

		MatrixStack matrixStack = new MatrixStack();
		matrixStack.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z); // <- probably dumb??
		matrixStack.translate(0.005, 0.005, 0.005); // <- to prevent Z-fighting

		this.applyOffset(matrixStack, tickDelta);

		VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
//		VertexConsumerProvider.Immediate immediate2 = MinecraftClient.getInstance().getBufferBuilders().getBlockBufferBuilders().
		this.RENDERER.getModelRenderer().render(
				world,
				this.RENDERER.getModel(blockState),
				blockState,
				this.POSITION,
				matrixStack,
				immediate.getBuffer(RenderLayers.getMovingBlockLayer(blockState)),
				false,
				Random.create(),
				blockState.getRenderingSeed(this.POSITION),
				OverlayTexture.DEFAULT_UV
		);
//		this.RENDERER.renderBlockAsEntity(
//				blockState,
//				matrixStack,
//				immediate,
//				this.lightLevel,
//				OverlayTexture.DEFAULT_UV
//		);
//		immediate.draw();
	}

	public void applyOffset(MatrixStack matrices, float tickDelta) {
		float progress = Math.min(1, (tickDelta + this.age) / this.maxAge);
		float offset = 0.5F * (1 - (2 * ((progress - 1) * (progress - 1)) - 1) * (2 * ((progress - 1) * (progress - 1)) - 1));
		matrices.translate(offset * this.BUMP_UNIT_VECTOR.x, offset * this.BUMP_UNIT_VECTOR.y, offset * this.BUMP_UNIT_VECTOR.z);
		this.lastOffset = offset;
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.CUSTOM;
	}

	public static class Factory implements ParticleFactory<SimpleParticleType> {
		@Override
		public @Nullable Particle createParticle(SimpleParticleType parameters, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
			BlockPos position = BlockPos.ofFloored(x, y, z);
			return new BumpedBlockParticle(world, position, world.getBlockState(position), Direction.UP);
		}
	}
}
