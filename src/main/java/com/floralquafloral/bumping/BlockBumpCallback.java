package com.floralquafloral.bumping;

import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Callback for Mario bumping a block in the world.
 * Used on the client and server.
 * On the client, called before the block is visibly displaced and the attempt is networked to the server.
 * On the server, called before the block is given a redstone signal.
 * Upon return:
 * - PASS falls back to further processing.
 * - DISPLACE cancels further processing and displaces the block.
 * - BREAK cancels further processing and destroys the block, dropping an item.
 * - CANCEL cancels further processing and does not bump the block (or network the attempt!!!).
 * - CANCEL_NETWORKED cancels further processing and does not visually displace the block, but will still be networked
 *   if it occurs on the client.
 */
public interface BlockBumpCallback {
	Event<BlockBumpCallback> EVENT = EventFactory.createArrayBacked(BlockBumpCallback.class,
			listeners -> (marioData, marioClientData, marioTravelData, world, blockPos, blockState, strength, modifier, direction) -> {
				for(BlockBumpCallback listener : listeners) {
					BlockBumpResult result = listener.bump(
							marioData, marioClientData, marioTravelData,
							world, blockPos, blockState,
							strength, modifier, direction
					);
					if(result != BlockBumpResult.PASS) return result;
				}


				return BlockBumpResult.PASS;
			}
	);

	BlockBumpResult bump(
			MarioData marioData, @Nullable MarioClientSideData marioClientData, @Nullable MarioTravelData marioTravelData,
			World world, BlockPos blockPos, BlockState blockState,
			int strength, int modifier, Direction direction
	);
}
