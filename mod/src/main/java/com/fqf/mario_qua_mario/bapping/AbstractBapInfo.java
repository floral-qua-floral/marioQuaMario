package com.fqf.mario_qua_mario.bapping;

import com.fqf.mario_qua_mario_api.interfaces.BapResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Set;

public abstract class AbstractBapInfo {
	public final World WORLD;
	public final BlockPos POS;
	public final BapResult RESULT;

	public AbstractBapInfo(World world, BlockPos pos, BapResult result) {
		this.WORLD = world;
		this.POS = pos;
		this.RESULT = result;
	}

	public abstract Set<Map<World, Set<BlockPos>>> getFastLists(boolean isAdding);

	public abstract void tick();

	public abstract boolean isDone();

	public abstract AbstractBapInfo finish();
}
