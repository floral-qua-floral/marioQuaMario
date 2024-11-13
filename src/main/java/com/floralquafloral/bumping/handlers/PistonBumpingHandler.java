package com.floralquafloral.bumping.handlers;

import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import net.minecraft.block.*;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PistonBumpingHandler implements BumpingHandler {
	public static boolean forceRetract;

	@Override
	public @Nullable BumpLegality evaluateBumpLegality(BlockState state, BlockView world, BlockPos pos, int strength, Direction direction) {
		if(state.isOf(Blocks.PISTON_HEAD) && state.get(Properties.FACING) == Direction.UP) {
			return BumpLegality.BUMP;
		}
		return null;
	}

	@Override
	public boolean bumpResponseCommon(MarioData data, @Nullable MarioTravelData travelData, World world, BlockState state, BlockPos pos, int baseStrength, int modifiedStrength, Direction direction) {
		return false;
	}

	@Override
	public boolean bumpResponseClients(MarioClientSideData data, ClientWorld world, BlockState state, BlockPos pos, int baseStrength, int modifiedStrength, Direction direction) {
		return false;
	}
}
