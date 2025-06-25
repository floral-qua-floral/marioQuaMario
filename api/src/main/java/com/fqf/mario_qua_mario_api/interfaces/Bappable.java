package com.fqf.mario_qua_mario_api.interfaces;

import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public interface Bappable {
	default BapResult mqm$getBapResult(
			IMarioData data,
			World world,
			BlockState blockState,
			Direction direction,
			int strength
	) {
		return BapResult.BUMP_EMBRITTLE;
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
