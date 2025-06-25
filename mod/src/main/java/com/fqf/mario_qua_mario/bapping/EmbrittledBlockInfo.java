package com.fqf.mario_qua_mario.bapping;

import com.fqf.mario_qua_mario_api.interfaces.BapResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Set;

public class EmbrittledBlockInfo extends AbstractBapInfo {
	protected final long FINISH_TIME;
	public EmbrittledBlockInfo(World world, BlockPos pos) {
		super(world, pos, BapResult.EMBRITTLE);
		this.FINISH_TIME = this.WORLD.getTime() + 40;
	}

	@Override
	public Set<Map<World, Set<BlockPos>>> getFastLists(boolean isAdding) {
		return Set.of(BlockBappingUtil.BRITTLE_BLOCK_POSITIONS);
	}

	@Override
	public void tick() {

	}

	@Override
	public boolean isDone() {
		return this.WORLD.getTime() > this.FINISH_TIME;
	}

	@Override
	public AbstractBapInfo finish() {
		BlockBappingUtil.BRITTLE_BLOCK_POSITIONS.get(this.WORLD).remove(this.POS);
		return null;
	}
}
