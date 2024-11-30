package com.floralquafloral.bumping;

import com.floralquafloral.BlockBumpCallback;
import com.floralquafloral.BlockBumpResult;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.mariodata.moveable.MarioMoveableData;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

import static com.floralquafloral.MarioQuaMario.MOD_ID;

public class BlockBumpHandler {
	public static final TagKey<Block> UNBUMPABLE =
			TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "unbumpable"));
	public static final TagKey<Block> BUMP_REGARDLESS_OF_HARDNESS =
			TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "bump_regardless_of_hardness"));
	public static final TagKey<Block> EXTREMELY_EASY_TO_BUMP =
			TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "extremely_easy_to_bump"));
	public static final TagKey<Block> UNBREAKABLE_FROM_BUMPING =
			TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "unbreakable_from_bumping"));

	public static final TagKey<Block> DO_NOT_POWER =
			TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "do_not_power_on_bump"));

	public static final Set<BlockPos> FORCED_SIGNALS = new HashSet<>();
	public static final Set<ForcedSignalSpot> FORCED_SIGNALS_DATA = new HashSet<>();
	public static final Event<BlockBumpCallback> EVENT = EventFactory.createArrayBacked(BlockBumpCallback.class,
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

	public static BlockBumpResult processBumpResult(
			MarioData marioData, @Nullable MarioClientSideData marioClientData, @Nullable MarioTravelData marioTravelData,
			World world, BlockPos blockPos, BlockState blockState,
			int strength, int modifier, Direction direction
	) {
		BlockBumpResult result = getBumpResult(
				marioData, marioClientData, marioTravelData,
				world, blockPos, blockState,
				strength, modifier, direction
		);

		if(result == BlockBumpResult.PASS) {
			CrashReport crashReport = CrashReport.create(new InvalidBumpResultException(), "Bumping a block");
			throw new CrashException(crashReport);
		}

		if(result == BlockBumpResult.BREAK) world.breakBlock(blockPos, true, marioData.getMario());
		else if(result == BlockBumpResult.DISPLACE) {
			// Apply redstone power
			if(!blockState.isIn(BlockBumpHandler.DO_NOT_POWER)) {
				BlockBumpHandler.FORCED_SIGNALS.add(blockPos);
				BlockBumpHandler.FORCED_SIGNALS_DATA.add(new BlockBumpHandler.ForcedSignalSpot(blockPos, world));
				world.updateNeighbor(blockPos, blockState.getBlock(), blockPos);
			}
		}

		if(marioTravelData != null) ((MarioMoveableData) marioTravelData).applyModifiedVelocity();
		return result;
	}

	private static class InvalidBumpResultException extends RuntimeException {}

	private static BlockBumpResult getBumpResult(
			MarioData marioData, @Nullable MarioClientSideData marioClientData, @Nullable MarioTravelData marioTravelData,
			World world, BlockPos blockPos, BlockState blockState,
			int strength, int modifier, Direction direction
	) {
		BlockBumpResult result = EVENT.invoker().bump(
				marioData, marioClientData, marioTravelData,
				world, blockPos, blockState,
				strength, modifier, direction
		);
		if (result != BlockBumpResult.PASS)
			return result;
		else {
			if(blockState.isIn(BlockBumpHandler.UNBUMPABLE)) return BlockBumpResult.CANCEL;
			if(blockState.isIn(BlockBumpHandler.BUMP_REGARDLESS_OF_HARDNESS) && strength >= 4) return BlockBumpResult.DISPLACE;
			if(blockState.isIn(BlockBumpHandler.EXTREMELY_EASY_TO_BUMP) && strength >= 1) return BlockBumpResult.DISPLACE;

			int modifiedStrength = strength + modifier;

			float adjustedHardness = blockState.getHardness(world, blockPos);
			if(blockState.isTransparent(world, blockPos) && !blockState.hasSidedTransparency()) adjustedHardness *= 0.5F;

			if(adjustedHardness == -1 || modifiedStrength <= 1) return BlockBumpResult.CANCEL;

			BlockBumpResult strongEnoughToBreakResult =
					blockState.isIn(UNBREAKABLE_FROM_BUMPING) ? BlockBumpResult.DISPLACE : BlockBumpResult.BREAK;

			// Super Mario spin-jumping can destroy fairly fragile blocks (ice, leaves).
			// Failing to destroy a block in this manner won't bump it at all.
			if(modifiedStrength == 2)
				return (adjustedHardness <= 0.25F) ? strongEnoughToBreakResult : BlockBumpResult.CANCEL;

			// Bumps with base strength 3 will bump blocks the same way as strength 4, but are much less capable of breaking blocks
			strength = Math.max(strength, 4);

			BlockBumpResult failedToBreakResult = (adjustedHardness <= 0.75F * Math.max(strength, modifiedStrength))
					? BlockBumpResult.DISPLACE : BlockBumpResult.CANCEL;

			// Small Mario ground-pounding or bopping a ceiling can only break exceptionally fragile blocks (candles, moss, scaffolding).
			if(modifiedStrength == 3)
				return (adjustedHardness < 0.2F) ? strongEnoughToBreakResult : failedToBreakResult;

			// If we get to this point, we know for sure modifiedStrength >= 3.
			// Super Mario gets a bonus to breaking bricks
			if(blockState.getBlock().toString().contains("brick")) adjustedHardness -= 1;

			// Super Mario ground-pounding or bopping a ceiling can break somewhat fragile blocks (dirt, pumpkins),
			// and relatively weak brick blocks (mud bricks, stone bricks, NOT end bricks)
			return (adjustedHardness <= modifiedStrength * 0.25F) ? strongEnoughToBreakResult : failedToBreakResult;
		}
	}

	public static class ForcedSignalSpot {
		public final BlockPos POSITION;
		public final World WORLD;
		public int delay;

		private ForcedSignalSpot(BlockPos position, World world) {
			this.POSITION = position;
			this.WORLD = world;
			this.delay = 3;
		}
	}
}
