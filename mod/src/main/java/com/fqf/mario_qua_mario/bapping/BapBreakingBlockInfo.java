package com.fqf.mario_qua_mario.bapping;

import com.fqf.mario_qua_mario_api.interfaces.BapResult;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Set;

public class BapBreakingBlockInfo extends BumpingBlockInfo {


	public BapBreakingBlockInfo(World world, BlockPos pos, Direction direction, Entity bapper) {
		super(world, pos, direction, bapper, BapResult.BREAK);
	}

	@Override
	public Set<Map<World, Set<BlockPos>>> getFastLists(boolean isAdding) {
		return Set.of(BlockBappingUtil.HIDDEN_BLOCK_POSITIONS);
	}

	@Override
	protected long getFinishTime(World world) {
		return world.getTime() + (BumpingBlockInfo.BUMP_DURATION / 2);
	}

	@Override
	public AbstractBapInfo finish() {
		super.finish();
		this.WORLD.breakBlock(this.POS, true, this.BAPPER);
		return null;
	}
}
