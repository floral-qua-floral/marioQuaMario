package com.floralquafloral;

import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import com.floralquafloral.util.MarioSFX;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public abstract class BlockBumping {
	public static final Map<BlockPos, BumpedBlockData> BUMPED_BLOCKS = new HashMap<>();
	public static BlockPos SAVED_POSITION = null;

	public static void registerPackets() {

	}
	public static void registerPacketsClient() {

	}

	public static void attempt(MarioMainClientData data, Vec3d movement) {

	}

	public static void bumpBlocks(MarioMainClientData data, ClientWorld world, Iterable<BlockPos> blocks, Direction direction, int strength) {
		int bumpCount = 0;
		double bumpX = 0, bumpY = 0, bumpZ = 0;
		for(BlockPos bumpPos : blocks) {
			bumpPos = new BlockPos(bumpPos);
			BUMPED_BLOCKS.put(bumpPos, new BumpedBlockData());
			Vec3d centerPos = bumpPos.toCenterPos();
			bumpCount++;
			bumpX += centerPos.x; bumpY += centerPos.y; bumpZ += centerPos.z;
			BlockState bumpBlockState = world.getBlockState(bumpPos);
			world.updateListeners(bumpPos, bumpBlockState, bumpBlockState, Block.NOTIFY_ALL);
		}

		if(bumpCount > 0) {
			long seed = Random.create().nextLong();
			data.playSoundEvent(
					MarioSFX.BUMP, SoundCategory.BLOCKS,
					bumpX / bumpCount, bumpY / bumpCount, bumpZ / bumpCount,
					1.0F, 1.0F, seed
			);
		}
	}

	public static class BumpedBlockData {

	}
}
