package com.fqf.mario_qua_mario_api.interfaces;

import com.fqf.mario_qua_mario_api.MarioQuaMarioAPI;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public interface Bappable {
	default BapResult mqm$getBapResult(
			IMarioData data,
			World world,
			BlockPos pos, BlockState blockState,
			Direction direction,
			int strength
	) {
		if(strength >= 1 && this instanceof Block thisBlock) {
			float hardness = blockState.getHardness(world, pos);

			// Cannot bump indestructible blocks
			if(hardness == -1)
				return BapResult.FAIL;

			float bustThreshold = strength * 0.1F;
			if(hardness < bustThreshold)
				return BapResult.BUST;

			float breakThreshold = strength * 0.125F;
			if(hardness < breakThreshold)
				return BapResult.BREAK;

			float embrittleThreshold = strength * 0.375F;
			if(hardness < embrittleThreshold)
				return BapResult.BUMP_EMBRITTLE;

			float bumpThreshold = switch(strength) {
				case 1, 2 -> 0;
				case 3 -> 3;
				default -> 1 + strength * 0.5F;
			};
			if(hardness < bumpThreshold)
				 return BapResult.BUMP;
		}
		return BapResult.FAIL;
	}

	/**
	 * @param data
	 * @param world
	 * @param blockState
	 * @param direction
	 * @param strength
	 * @param result
	 *
	 * After this is called, the block will receive an update.
	 */
	default void mqm$onBapped(
			IMarioData data,
			World world,
			BlockState blockState,
			Direction direction,
			int strength,
			BapResult result
	) {

	}
}
