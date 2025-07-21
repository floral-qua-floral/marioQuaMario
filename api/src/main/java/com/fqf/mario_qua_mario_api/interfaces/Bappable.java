package com.fqf.mario_qua_mario_api.interfaces;

import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.util.MQMTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
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
	default BapResult mqm$getBapResult(
			IMarioData data, World world,
			BlockPos pos, BlockState blockState, float hardness,
			Direction direction,
			int strength
	) {
		// Cannot affect indestructible blocks.
		if(hardness == -1 || blockState.isIn(MQMTags.UNBAPPABLE))
			return BapResult.FAIL;

		if(blockState.isIn(MQMTags.USES_DOUBLE_HARDNESS_WHEN_BAPPED))
			hardness *= 2;
		if(blockState.isIn(MQMTags.USES_HALF_HARDNESS_WHEN_BAPPED))
			hardness *= 0.5F;

		float bustThreshold = strength * 0.1F;
		if(hardness < bustThreshold)
			return BapResult.BUST;

		boolean shouldPower = !blockState.isIn(MQMTags.NOT_POWERED_WHEN_BAPPED);

		float breakThreshold = strength * 0.125F;
		if(hardness < breakThreshold)
			return shouldPower ? BapResult.BREAK : BapResult.BREAK_NO_POWER;

		float embrittleThreshold = strength * 0.375F;
		if(hardness < embrittleThreshold)
			return shouldPower ? BapResult.BUMP_EMBRITTLE : BapResult.BUMP_EMBRITTLE_NO_POWER;

		float bumpThreshold = switch(strength) {
			case 1, 2 -> 0;
			case 3 -> 3;
			default -> 1 + strength * 0.5F;
		};
		if(hardness < bumpThreshold)
			return shouldPower ? BapResult.BUMP : BapResult.BUMP_NO_POWER;

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
	default void mqm$onBapped(
			IMarioData data,
			World world,
			BlockPos pos, BlockState blockState,
			Direction direction,
			int strength,
			BapResult result
	) {

	}

	private static TagKey<Block> getBlockTag(String name) {
		return TagKey.of(RegistryKeys.BLOCK, Identifier.of("mario_qua_mario", name));
	}
}
