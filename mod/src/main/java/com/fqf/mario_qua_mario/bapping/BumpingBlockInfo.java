package com.fqf.mario_qua_mario.bapping;

import com.fqf.mario_qua_mario_api.interfaces.BapResult;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BumpingBlockInfo extends AbstractBapInfo {
	public static final int BUMP_DURATION = 6;

	protected final long FINISH_TIME;
	public final Direction DISPLACEMENT_DIRECTION;

	protected BumpingBlockInfo(World world, BlockPos pos, BapResult result, Direction direction, Entity bapper) {
		super(world, pos, result, bapper);
		this.FINISH_TIME = this.getFinishTime(world);
		this.DISPLACEMENT_DIRECTION = direction;
	}

	protected long getFinishTime(World world) {
		return world.getTime() + BUMP_DURATION;
	}

	@Override
	public void tick() {

	}

	@Override
	public boolean isDone() {
		return this.WORLD.getTime() > this.FINISH_TIME;
//		return false;
	}

	@Override
	public AbstractBapInfo finishAndGetReplacement() {
		return null;
	}
}
