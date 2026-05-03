package com.fqf.charapoweract_api.interfaces;

import com.fqf.charapoweract_api.cpadata.ICPAData;
import com.fqf.charapoweract_api.util.CPATags;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public interface Bappable {
	/**
	 * @param data
	 * @param world
	 * @param pos
	 * @param blockState
	 * @param hardness The block's hardness. Unlike the BlockState.getHardness method, this does not change when
	 *                 the block is embrittled.
	 * @param direction
	 * @param strength
	 * @return How the block should react to the collision.
	 */
	default BapResult cpa$getBapResult(
			ICPAData data, World world,
			BlockPos pos, BlockState blockState, float hardness,
			Direction direction,
			int strength
	) {
		// Cannot affect indestructible blocks.
		if(hardness == -1 || blockState.isIn(CPATags.UNBAPPABLE))
			return BapResult.FAIL;

		if(blockState.isIn(CPATags.USES_DOUBLE_HARDNESS_WHEN_BAPPED))
			hardness *= 2;
		if(blockState.isIn(CPATags.USES_HALF_HARDNESS_WHEN_BAPPED))
			hardness *= 0.5F;

		float bustThreshold;
		float breakThreshold;
		float embrittleThreshold;
		float bumpThreshold;

		switch(strength) {
			case 1 -> {
				return BapResult.FAIL;
			}

			case 2 -> {
				// Small Mario minor bap
				bustThreshold = 0.2F;
				breakThreshold = 0.3F;
				embrittleThreshold = 0.4F;
				bumpThreshold = 1.1F;
			}

			case 3 -> {
				// Small Mario major bap
				// Super Mario minor bap
				bustThreshold = 0.2F;
				breakThreshold = 0.4F;
				embrittleThreshold = 0.6F;
				bumpThreshold = 2.6F;
			}

			case 4 -> {
				// Super Mario major bap
				// Elephant Mario minor bap???
				bustThreshold = 0.25F;
				breakThreshold = 0.6F;
				embrittleThreshold = 1.55F;
				bumpThreshold = 3.5F;
			}

			default -> throw new IllegalStateException("Unexpected bap strength value: " + strength);
		}

		if(hardness < bustThreshold)
			return BapResult.BUST;

		boolean shouldPower = !blockState.isIn(CPATags.NOT_POWERED_WHEN_BAPPED);

		if(hardness < breakThreshold)
			return shouldPower ? BapResult.BREAK : BapResult.BREAK_WITHOUT_POWERING;

		if(hardness < embrittleThreshold)
			return shouldPower ? BapResult.BUMP_EMBRITTLE : BapResult.BUMP_EMBRITTLE_WITHOUT_POWERING;

		if(hardness < bumpThreshold)
			return shouldPower ? BapResult.BUMP : BapResult.BUMP_WITHOUT_POWERING;

		return BapResult.FAIL;
	}

	/**
	 * @param data
	 * @param world
	 * @param pos
	 * @param blockState
	 * @param direction
	 * @param strength
	 * @param result     After this is called, the block will receive an update.
	 */
	default void cpa$onBapped(
			ICPAData data,
			World world,
			BlockPos pos, BlockState blockState,
			Direction direction,
			int strength,
			BapResult result
	) {

	}
}
