package com.floralquafloral.bumping.handlers;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.floralquafloral.MarioQuaMario.MOD_ID;

public class BaselineBumpingHandler implements BumpingHandler {
	public static final TagKey<Block> BUMP_REGARDLESS_OF_HARDNESS =
			TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "bump_regardless_of_hardness"));
	public static final TagKey<Block> UNBUMPABLE = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "unbumpable"));
	public static final TagKey<Block> EXTREMELY_EASY_TO_BUMP = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "extremely_easy_to_bump"));

	@Override @NotNull
	public BumpLegality evaluateBumpLegality(BlockState state, BlockView world, BlockPos pos, int strength, Direction direction) {
		if(state.isIn(UNBUMPABLE)) return BumpLegality.IGNORE;
		if(state.isIn(BUMP_REGARDLESS_OF_HARDNESS) && strength >= 4) return BumpLegality.BUMP;
		if(state.isIn(EXTREMELY_EASY_TO_BUMP) && strength >= 1) return BumpLegality.BUMP;


		MarioQuaMario.LOGGER.info("BASELINE BUMPING HANDLER:"
				+ "\nBlock: " + state.getBlock()
				+ "\nHardness: " + state.getHardness(world, pos)
		);


		return BumpLegality.IGNORE;
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
