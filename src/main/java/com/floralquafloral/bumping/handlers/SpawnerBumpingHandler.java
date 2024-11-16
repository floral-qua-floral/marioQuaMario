package com.floralquafloral.bumping.handlers;

import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.block.TrialSpawnerBlock;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.entity.TrialSpawnerBlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SpawnerBumpingHandler implements BumpingHandler {
	@Override
	public @Nullable BumpLegality evaluateBumpLegality(BlockState state, BlockView world, BlockPos pos, int strength, Direction direction) {
		if(state.isOf(Blocks.SPAWNER) || state.isOf(Blocks.TRIAL_SPAWNER)) {
			return BumpLegality.BUMP;
		}
		return null;
	}

	@Override
	public boolean bumpResponseCommon(MarioData data, @Nullable MarioTravelData travelData, World world, BlockState state, BlockPos pos, int baseStrength, int modifiedStrength, Direction direction) {
		if(world instanceof ServerWorld serverWorld) {
			if(world.getBlockEntity(pos) instanceof MobSpawnerBlockEntity spawnerEntity) {
				for(int incrementeroo = 0; incrementeroo < 7; incrementeroo++) {
					spawnerEntity.getLogic().serverTick(serverWorld, pos);
				}
				return true;
			}

			if(world.getBlockEntity(pos) instanceof TrialSpawnerBlockEntity trialSpawnerEntity) {
				for(int incrementeroo = 0; incrementeroo < 3; incrementeroo++) {
					trialSpawnerEntity.getSpawner().trySpawnMob(serverWorld, pos);
				}
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean bumpResponseClients(MarioClientSideData data, ClientWorld world, BlockState state, BlockPos pos, int baseStrength, int modifiedStrength, Direction direction) {
		return false;
	}
}
