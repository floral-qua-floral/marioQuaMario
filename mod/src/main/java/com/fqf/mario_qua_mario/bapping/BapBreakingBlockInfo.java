package com.fqf.mario_qua_mario.bapping;

import com.fqf.mario_qua_mario_api.interfaces.BapResult;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BapBreakingBlockInfo extends BumpingBlockInfo {
	public BapBreakingBlockInfo(World world, BlockPos pos, BapResult result, Direction direction, Entity bapper) {
		super(world, pos, result, direction, bapper);
	}

	@Override
	protected long getFinishTime(World world) {
		return world.getTime() + (BumpingBlockInfo.BUMP_DURATION / 2) + (world.isClient() ? 0 : -1);
	}

	@Override
	public AbstractBapInfo finishAndGetReplacement() {
		super.finishAndGetReplacement();
		this.WORLD.breakBlock(this.POS, true, this.BAPPER);
		return null;
	}
}
