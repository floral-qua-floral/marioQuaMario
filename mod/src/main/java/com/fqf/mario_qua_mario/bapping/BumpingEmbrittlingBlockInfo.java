package com.fqf.mario_qua_mario.bapping;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario_api.interfaces.BapResult;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Set;

public class BumpingEmbrittlingBlockInfo extends BumpingBlockInfo {
	public BumpingEmbrittlingBlockInfo(World world, BlockPos pos, Direction direction, Entity bapper) {
		super(world, pos, direction, bapper, BapResult.BUMP_EMBRITTLE);

//		MarioQuaMario.LOGGER.info("Embrittling block @ {}", pos);
	}

	@Override
	public Set<Map<World, Set<BlockPos>>> getFastLists(boolean isAdding) {
		return isAdding ? Set.of(
				BlockBappingUtil.HIDDEN_BLOCK_POSITIONS,
				BlockBappingUtil.BRITTLE_BLOCK_POSITIONS,
				BlockBappingUtil.POWERED_BLOCK_POSITIONS
		) : Set.of(
				BlockBappingUtil.HIDDEN_BLOCK_POSITIONS,
				BlockBappingUtil.POWERED_BLOCK_POSITIONS
		);
	}

	@Override
	public AbstractBapInfo finish() {
		super.finish();
		BlockBappingUtil.BRITTLE_BLOCK_POSITIONS.get(this.WORLD).remove(this.POS);
		return new EmbrittledBlockInfo(this.WORLD, this.POS, this.BAPPER);
	}
}
