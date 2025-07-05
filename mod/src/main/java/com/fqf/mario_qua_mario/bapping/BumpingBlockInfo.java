package com.fqf.mario_qua_mario.bapping;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario_api.interfaces.BapResult;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Set;

public class BumpingBlockInfo extends AbstractBapInfo {
	public static final int BUMP_DURATION = 6;

	protected final long FINISH_TIME;
	public final Direction DISPLACEMENT_DIRECTION;

	public BumpingBlockInfo(World world, BlockPos pos, Direction direction, Entity bapper) {
		this(world, pos, direction, bapper, BapResult.BUMP);
	}

	protected BumpingBlockInfo(World world, BlockPos pos, Direction direction, Entity bapper, BapResult result) {
		super(world, pos, result, bapper);
		this.FINISH_TIME = this.getFinishTime(world);
		this.DISPLACEMENT_DIRECTION = direction;
	}

	protected long getFinishTime(World world) {
		return world.getTime() + BUMP_DURATION;
	}

	@Override
	public Set<Map<World, Set<BlockPos>>> getFastLists(boolean isAdding) {
		return Set.of(BlockBappingUtil.HIDDEN_BLOCK_POSITIONS, BlockBappingUtil.POWERED_BLOCK_POSITIONS);
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
	public AbstractBapInfo finish() {
		BlockBappingUtil.HIDDEN_BLOCK_POSITIONS.get(this.WORLD).remove(this.POS);
		Set<BlockPos> poweredBlocksInWorld = BlockBappingUtil.POWERED_BLOCK_POSITIONS.get(this.WORLD);
		if(poweredBlocksInWorld != null) poweredBlocksInWorld.remove(this.POS);
		this.WORLD.updateNeighbor(this.POS, this.WORLD.getBlockState(this.POS).getBlock(), this.POS);
		BlockBappingUtil.reRenderPos(this);
		return null;
	}
}
