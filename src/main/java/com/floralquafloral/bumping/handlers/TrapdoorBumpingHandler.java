package com.floralquafloral.bumping.handlers;

import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import net.minecraft.block.BlockState;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class TrapdoorBumpingHandler implements BumpingHandler {
	@Override
	public @Nullable BumpLegality evaluateBumpLegality(BlockState state, BlockView world, BlockPos pos, int strength, Direction direction) {
		if(state.isIn(BlockTags.TRAPDOORS)) {
			if(state.get(Properties.OPEN)) {
				// It's an open trapdoor; check to see if we'd be knocking it closed
				if (
						(state.get(Properties.BLOCK_HALF) == BlockHalf.TOP && direction == Direction.UP)
						|| (state.get(Properties.BLOCK_HALF) == BlockHalf.BOTTOM && direction == Direction.DOWN)
				) {
					return BumpLegality.BUMP;
				}
			}
			else {
				// It's a closed trapdoor; check if we'd be knocking it open
				if (
						(state.get(Properties.BLOCK_HALF) == BlockHalf.BOTTOM && direction == Direction.UP)
						|| (state.get(Properties.BLOCK_HALF) == BlockHalf.TOP && direction == Direction.DOWN)
				) {
					return BumpLegality.BUMP;
				}
			}
		}
		return null;
	}

	@Override
	public boolean bumpResponseCommon(MarioData data, @Nullable MarioTravelData travelData, World world, BlockState state, BlockPos pos, int baseStrength, int modifiedStrength, Direction direction) {
		if(state.isIn(BlockTags.TRAPDOORS)) {
			if(state.get(Properties.OPEN)) {
				// It's an open trapdoor; check to see if we'd be knocking it closed
				if (
						(state.get(Properties.BLOCK_HALF) == BlockHalf.TOP && direction == Direction.UP)
								|| (state.get(Properties.BLOCK_HALF) == BlockHalf.BOTTOM && direction == Direction.DOWN)
				) {
//					state.cycle(Properties.OPEN);
					((TrapdoorBlock) state.getBlock()).flip(state, world, pos, data.getMario());
					return true;
				}
			}
			else {
				// It's a closed trapdoor; check if we'd be knocking it open
				if (
						(state.get(Properties.BLOCK_HALF) == BlockHalf.BOTTOM && direction == Direction.UP)
								|| (state.get(Properties.BLOCK_HALF) == BlockHalf.TOP && direction == Direction.DOWN)
				) {
					((TrapdoorBlock) state.getBlock()).flip(state, world, pos, data.getMario());
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean bumpResponseClients(MarioClientSideData data, ClientWorld world, BlockState state, BlockPos pos, int baseStrength, int modifiedStrength, Direction direction) {
		return false;
	}
}
