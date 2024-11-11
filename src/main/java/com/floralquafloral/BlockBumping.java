package com.floralquafloral;

import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.Map;

public abstract class BlockBumping {
	public static final Map<BlockPos, BumpedBlockData> BUMPED_BLOCKS = new HashMap<>();

	public static void registerPackets() {

	}
	public static void registerPacketsClient() {

	}

	public static void attempt(MarioMainClientData data, Vec3d movement) {

	}

	public static void bumpBlock(MarioData data, BlockPos position, boolean isCeiling, int strength) {
		MarioQuaMario.LOGGER.info("{} bumped block {}", data.getMario(), position);
	}

	public static class BumpedBlockData {

	}
}
