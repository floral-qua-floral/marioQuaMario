package com.fqf.charapoweract.bapping;

import com.fqf.charapoweract.CharaPowerAct;
import com.fqf.charapoweract.compat.optional.SableCompatSafe;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayVertexConsumer;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.ModelLoader;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniondc;
import org.joml.Quaternionf;
import org.joml.Vector3dc;

public class BlockBappingClientUtil {
	public static void clientWorldTick(ClientWorld world) {
		BlockBappingUtil.commonWorldTick(world);
	}

	public static void renderBumpedBlock(ClientWorld world, WorldBapsInfo worldBaps, MatrixStack matrixStack, Vec3d cameraPos, BlockPos pos, boolean checkBrittle) {
		Vec3d bumpOffset = BlockBappingClientUtil.calculateDubiousOffset(worldBaps, pos, BlockBappingClientUtil.getTickDelta());

		Triple<Vec3d, Vector3dc, Quaternionf> bumpedBlockPosAndOrientation = SableCompatSafe.getPosAndOrientation(world, pos);
		Vec3d bumpedBlockPos = bumpedBlockPosAndOrientation.getLeft();
		Vector3dc bumpedBlockScale = bumpedBlockPosAndOrientation.getMiddle();
		Quaternionf bumpedBlockOrientation = bumpedBlockPosAndOrientation.getRight();

		double diffX = bumpedBlockPos.x + bumpOffset.x - cameraPos.x;
		double diffY = bumpedBlockPos.y + bumpOffset.y - cameraPos.y;
		double diffZ = bumpedBlockPos.z + bumpOffset.z - cameraPos.z;

		if(diffX * diffX + diffY * diffY + diffZ * diffZ < 1024) {
			matrixStack.push();

			float inherentScale = CharaPowerAct.CONFIG.getInherentBumpedBlockScale();
			float inherentOffset = (1 - inherentScale) / 2;

			matrixStack.translate(diffX + inherentOffset, diffY + inherentOffset, diffZ + inherentOffset);
			matrixStack.multiply(bumpedBlockOrientation);
			matrixStack.scale(
					(float) (inherentScale * bumpedBlockScale.x()),
					(float) (inherentScale * bumpedBlockScale.y()),
					(float) (inherentScale * bumpedBlockScale.z())
			);

			if(checkBrittle && worldBaps.BRITTLE.contains(pos)) {
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
			}

			VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
			MinecraftClient.getInstance().getBlockRenderManager().renderBlock(
					world.getBlockState(pos),
					pos,
					world,
					matrixStack,
					immediate.getBuffer(RenderLayers.getMovingBlockLayer(world.getBlockState(pos))),
					false,
					world.getRandom()
			);

			BlockEntity entity = world.getBlockEntity(pos);
			if(entity != null) {
				MinecraftClient.getInstance().getBlockEntityRenderDispatcher().render(entity, getTickDelta(), matrixStack, immediate);
			}

			matrixStack.pop();
		}
	}

	public static float getTickDelta() {
		return MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(false);
	}

	public static double calculateOffsetMagnitude(BumpingBlockInfo info, float tickDelta) {
		int ticksAlive = BumpingBlockInfo.BUMP_DURATION - (int) (info.FINISH_TIME - info.WORLD.getTime()) - 1;
		float progress = Math.min(1, (tickDelta + ticksAlive) / BumpingBlockInfo.BUMP_DURATION);
		return 0.5F * (1 - (2 * ((progress - 1) * (progress - 1)) - 1) * (2 * ((progress - 1) * (progress - 1)) - 1));
	}

	public static Vec3d calculateOffset(BumpingBlockInfo info, float tickDelta) {
		return new Vec3d(info.DISPLACEMENT_DIRECTION.getUnitVector()).multiply(calculateOffsetMagnitude(info, tickDelta));
	}

	public static Vec3d calculateDubiousOffset(World world, BlockPos pos, float tickDelta) {
		WorldBapsInfo worldInfo = BlockBappingUtil.getBapsInfoNullable(world);
		if(worldInfo == null) return Vec3d.ZERO;
		else return calculateDubiousOffset(worldInfo, pos, tickDelta);
	}

	public static Vec3d calculateDubiousOffset(WorldBapsInfo worldInfo, BlockPos pos, float tickDelta) {
		AbstractBapInfo bapInfo = worldInfo.ALL_BAPS.get(pos);
		if(bapInfo instanceof BumpingBlockInfo bumpInfo) {
			return calculateOffset(bumpInfo, tickDelta);
		}
		return Vec3d.ZERO;
	}

	public static Vec3d calculateDubiousOffsetUnder(Entity entity, float tickDelta) {
		if(!entity.isOnGround()) return Vec3d.ZERO;
		return calculateDubiousOffset(entity.getWorld(), entity.getVelocityAffectingPos(), tickDelta);
	}
}
