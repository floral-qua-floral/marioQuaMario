package com.floralquafloral.bumping.handlers;

import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public interface BumpingHandler {
	/**
	 * @param strength The inherent power of whatever Mario is doing to the block.
	 *                 4: Equivalent to a Ground Pound, or hitting a block from below
	 *                 (This would bump a Brick Block)
	 *                 2: Equivalent to landing on a block from a Spin Jump
	 *                 (This would trigger a silent reaction from a Flip Block)
	 *                 1: Equivalent to landing on a block from a normal jump
	 *                 (This would be ignored by most blocks)
	 * @return How the block responds to being interacted with in that way.
	 * Returning null means you're leaving it up to other handlers.
	 */
	@Nullable BumpLegality evaluateBumpLegality(
			BlockState state, BlockView world, BlockPos pos,
			int strength, Direction direction);

	enum BumpLegality {
		BUMP,
		SILENT_REACTION,
		IGNORE
	}

	/**
	 * This method should be used for common-side effects, such as breaking blocks.
	 * It's also conditionally used for affecting Mario's movement.
	 * @param travelData Use this if Mario should be moved around in some way when interacting with the block.
	 *                   This will be null if the method is running on the client side for a block that was bumped
	 *                   by someone other than yourself, so just make sure it's not null before using it.
	 * @param baseStrength The inherent power of whatever Mario is doing to the block.
	 * @param modifiedStrength The actual force with which Mario is executing that action.
	 *                         Normally equal to baseStrength, but it's 1 lower when done by Small Mario.
	 * @return Whether there was any response. If false, other handlers will check for a response too.
	 */
	boolean bumpResponseCommon(
			MarioData data, @Nullable MarioTravelData travelData, World world,
			BlockState state, BlockPos pos,
			int baseStrength, int modifiedStrength, Direction direction
	);

	/**
	 * This method should be used for client-side effects, such as particles, sounds, voice lines from Mario, etc.
	 */
	boolean bumpResponseClients(
			MarioClientSideData data, ClientWorld world,
			BlockState state, BlockPos pos,
			int baseStrength, int modifiedStrength, Direction direction
	);
}
