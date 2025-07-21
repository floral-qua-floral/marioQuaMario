package com.fqf.mario_qua_mario.bapping;

import com.fqf.mario_qua_mario_api.interfaces.BapResult;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EmbrittledBlockInfo extends AbstractBapInfo {
	protected final long FINISH_TIME;
	public EmbrittledBlockInfo(World world, BlockPos pos, Entity bapper) {
		super(world, pos, BapResult.EMBRITTLE, bapper);
		this.FINISH_TIME = this.WORLD.getTime() + 40;
	}

	@Override
	public void tick() {

	}

	@Override
	public boolean isDone() {
		return this.WORLD.getTime() > this.FINISH_TIME;
	}

	@Override
	public AbstractBapInfo finishAndGetReplacement() {
		return null;
	}
}
