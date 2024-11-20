package com.floralquafloral;

import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioTravelData;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Callback for Mario bumping a block in the world. Used on the client and server.
 * <p>
 * <b>When used for the main client:</b> Called before the block is visibly displaced and the attempt is networked to
 * the server.
 * <p>
 * <b>When used on the server:</b> Called before the interaction is networked to nearby players.
 * <p>
 * <b>When used for other clients:</b> Called before the block is visibly displaced.
 * <p>
 * Upon return:
 * <p> - <u>PASS</u> falls back to further processing.
 * <p> - <u>DISPLACE</u> cancels further processing and displaces the block.
 * <p> - <u>BREAK</u> cancels further processing and destroys the block, dropping an item.
 * <p> - <u>CANCEL</u> cancels further processing and does not bump the block (or network the attempt!!!).
 * <p> - <u>CANCEL_NETWORKED</u> cancels further processing and does not visually displace the block, but will still be
 * networked to the server and other clients.
 */
public interface BlockBumpCallback {
	BlockBumpResult bump(
			MarioData marioData, @Nullable MarioClientSideData marioClientData, @Nullable MarioTravelData marioTravelData,
			World world, BlockPos blockPos, BlockState blockState,
			int strength, int modifier, Direction direction
	);
}
