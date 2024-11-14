package com.floralquafloral.bumping.handlers;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.tick.OrderedTick;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.floralquafloral.MarioQuaMario.MOD_ID;

public class BaselineBumpingHandler implements BumpingHandler {
	public static BlockPos forceRedstoneSignalAtPos;
	public static final Set<Pair<BlockPos, Integer>> DELAYED_TICKS = new HashSet<>();

	public static final Set<BlockPos> FORCED_SIGNALS = new HashSet<>();
	public static final Set<ForcedSignalSpot> FORCED_SIGNALS_DATA = new HashSet<>();

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

	public static final TagKey<Block> BUMP_REGARDLESS_OF_HARDNESS =
			TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "bump_regardless_of_hardness"));
	public static final TagKey<Block> UNBUMPABLE = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "unbumpable"));
	public static final TagKey<Block> EXTREMELY_EASY_TO_BUMP = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "extremely_easy_to_bump"));

	public static final TagKey<Block> DO_NOT_POWER = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "do_not_power_on_bump"));
	public static final TagKey<Block> UNBREAKABLE_FROM_BUMPING = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "unbreakable_from_bumping"));
//	public static final TagKey<Block> BRICK_BLOCKS = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "brick_blocks"));

	@Override @NotNull
	public BumpLegality evaluateBumpLegality(BlockState state, BlockView world, BlockPos pos, int strength, Direction direction) {
		if(state.isIn(UNBUMPABLE)) return BumpLegality.IGNORE;
		if(state.isIn(BUMP_REGARDLESS_OF_HARDNESS) && strength >= 4) return BumpLegality.BUMP;
		if(state.isIn(EXTREMELY_EASY_TO_BUMP) && strength >= 1) return BumpLegality.BUMP;

		float hardnessToolless = state.getHardness(world, pos);
		if(state.isToolRequired()) hardnessToolless *= 3.33333333333F;
		if(state.isTransparent(world, pos) && !state.hasSidedTransparency()) hardnessToolless *= 0.5F;
		if(state.isIn(BlockTags.AXE_MINEABLE)) hardnessToolless *= 3.6F;
		if(state.getBlock().toString().contains("brick")) hardnessToolless *= 0.5F;

		if(hardnessToolless == -1) return BumpLegality.IGNORE;

		if(strength <= 1) return BumpLegality.IGNORE;
		if(strength == 2 && hardnessToolless <= 0.25F) return BumpLegality.SILENT_REACTION;
		else if(hardnessToolless <= 2.5F * strength) return BumpLegality.BUMP;

		return BumpLegality.IGNORE;
	}

	@Override
	public boolean bumpResponseCommon(MarioData data, @Nullable MarioTravelData travelData, World world, BlockState state, BlockPos pos, int baseStrength, int modifiedStrength, Direction direction) {
		float hardnessToolless = state.getHardness(world, pos);
		if(state.isToolRequired()) hardnessToolless *= 3.33333333333F;
		if(state.isTransparent(world, pos) && !state.hasSidedTransparency()) hardnessToolless *= 0.5F;
		if(state.isIn(BlockTags.AXE_MINEABLE)) hardnessToolless *= 3.6F;
		if(state.getBlock().toString().contains("brick")) hardnessToolless *= 0.5F;

		if(!state.isIn(UNBREAKABLE_FROM_BUMPING) && hardnessToolless != -1) {
			if (modifiedStrength >= 4 && hardnessToolless <= 3.5 && world.breakBlock(pos, true, data.getMario()))
				return true;

			if (modifiedStrength >= 2 && hardnessToolless <= 0.3 && world.breakBlock(pos, true, data.getMario()))
				return true;
		}

		if(!state.isIn(DO_NOT_POWER)) {
			FORCED_SIGNALS.add(pos);
			FORCED_SIGNALS_DATA.add(new ForcedSignalSpot(pos, world));
			world.updateNeighbor(pos, state.getBlock(), pos);
		}

		return false;
	}

	@Override
	public boolean bumpResponseClients(MarioClientSideData data, ClientWorld world, BlockState state, BlockPos pos, int baseStrength, int modifiedStrength, Direction direction) {
		return false;
	}
}
