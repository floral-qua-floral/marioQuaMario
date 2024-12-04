package com.floralquafloral.bumping;

import com.floralquafloral.BlockBumpResult;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import com.floralquafloral.util.MarioSFX;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BumpManagerClient {
	public static final Map<BlockPos, BumpedBlockParticle> BUMPED_BLOCKS = new HashMap<>();
	public static final Set<BlockPos> HIDDEN_BLOCKS = new HashSet<>();
	private static final Set<BlockBumpingPlan> BLOCKS_TO_DISPLACE = new HashSet<>();
	@Nullable
	public static BumpedBlockParticle eyeAdjustmentParticle;
	private static BlockPos eyeAdjustmentParticlePos;

	public static void registerClientEventListeners() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			HIDDEN_BLOCKS.clear();
			BUMPED_BLOCKS.clear();
		});

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if(!BLOCKS_TO_DISPLACE.isEmpty()) {
				for(BlockBumpingPlan plan : BLOCKS_TO_DISPLACE) {
					visuallyBumpBlock(plan.world, plan.pos, plan.direction);
					if(plan.pos.equals(eyeAdjustmentParticlePos)) {
						eyeAdjustmentParticle = BUMPED_BLOCKS.get(plan.pos);
					}
				}
				BLOCKS_TO_DISPLACE.clear();
			}
			eyeAdjustmentParticlePos = null;
		});

		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register((context, hitResult) -> {
			assert hitResult != null;
			return hitResult.getType() != HitResult.Type.BLOCK || !HIDDEN_BLOCKS.contains(((BlockHitResult) hitResult).getBlockPos());
		});
	}

	public static void visuallyBumpBlock(ClientWorld world, BlockPos bumpPos, Direction direction) {
		HIDDEN_BLOCKS.add(bumpPos); // more performant to check than the HashMap i think?
		BlockState bumpBlockState = world.getBlockState(bumpPos);
		BumpedBlockParticle newParticle = new BumpedBlockParticle(world,bumpPos, bumpBlockState, direction);
		BumpedBlockParticle old = BUMPED_BLOCKS.put(bumpPos, newParticle);
		if(old != null && old.isAlive()) {
			old.replaced = true;
			old.markDead();
		}

		MinecraftClient.getInstance().particleManager.addParticle(newParticle);

		world.updateListeners(bumpPos, bumpBlockState, bumpBlockState, Block.NOTIFY_ALL);
	}

	public static void endBump(@Nullable BumpedBlockParticle particle, ClientWorld world, BlockPos pos, BlockState blockState) {
		if(particle != BUMPED_BLOCKS.get(pos)) return;
		HIDDEN_BLOCKS.remove(pos);
		BUMPED_BLOCKS.remove(pos);
		world.updateListeners(pos, blockState, blockState, Block.NOTIFY_ALL);
	}

	public static boolean attemptBumpBlocks(
			MarioMainClientData data, ClientWorld world,
			Iterable<BlockPos> blocks, Direction direction,
			int baseStrength
	) {
		data.canRepeatPound = false;
		Set<BlockPos> fullBlocksSet = new HashSet<>();
		for(BlockPos pos : blocks) fullBlocksSet.add(pos);

		Set<BlockPos> prunedBlocksSet = bumpBlocksClient(data, data, world, fullBlocksSet, baseStrength, direction);

		if(!prunedBlocksSet.isEmpty()) {
			ClientPlayNetworking.send(new BumpManager.BumpC2SPayload(prunedBlocksSet, direction, baseStrength));
			if (fullBlocksSet.size() == prunedBlocksSet.size() && direction == Direction.DOWN) {
				// this will cause eye height adjustment even if some blocks returned CANCEL_NETWORKED :(
				eyeAdjustmentParticlePos = prunedBlocksSet.stream().findFirst().get();
			}
		}
		return !fullBlocksSet.isEmpty();
	}

	public static Set<BlockPos> bumpBlocksClient(
			MarioClientSideData marioClientData, @Nullable MarioMainClientData marioMainClientData,
			ClientWorld world, Iterable<BlockPos> positions,
			int strength, Direction direction
	) {
		boolean playBumpSound = false;
		int modifier = marioClientData.getBumpStrengthModifier();

		Set<BlockPos> blocksBumped = new HashSet<>();
		Set<BlockSoundGroup> soundGroups = new HashSet<>();

		for(BlockPos pos : positions) {
			BlockState state = world.getBlockState(pos);

			BlockBumpResult result = BlockBumpHandler.processBumpResult(
					marioClientData, marioClientData, marioMainClientData,
					world, pos, state,
					strength, modifier, direction
			);

			if(result == BlockBumpResult.DISPLACE) {
				blocksBumped.add(pos);
				playBumpSound = true;
				soundGroups.add(state.getSoundGroup());
				if(marioMainClientData == null) visuallyBumpBlock(world, pos, direction);
				else BLOCKS_TO_DISPLACE.add(new BlockBumpingPlan(world, pos, state, direction));
			}
			else if(result == BlockBumpResult.BREAK) {
				blocksBumped.add(pos);
				playBumpSound = true;
			}
			else if (result == BlockBumpResult.CANCEL_NETWORKED) blocksBumped.add(pos);
		}

		double marioHeadY = marioClientData.getMario().getY() + marioClientData.getMario().getHeight();
		if(playBumpSound) marioClientData.playSoundEvent(
				MarioSFX.BUMP, SoundCategory.BLOCKS,
				marioClientData.getMario().getX(), marioHeadY, marioClientData.getMario().getZ(),
				1.0F, 0.2F, Random.create().nextLong()
		);
		for(BlockSoundGroup group : soundGroups) {
			marioClientData.playSoundEvent(
					group.getPlaceSound(), SoundCategory.BLOCKS,
					marioClientData.getMario().getX(), marioHeadY, marioClientData.getMario().getZ(),
					group.pitch * 0.8F /* why... */, group.volume, Random.create().nextLong()
			);
		}

		return blocksBumped;
	}

	private record BlockBumpingPlan(ClientWorld world, BlockPos pos, BlockState state, Direction direction) {}
}
