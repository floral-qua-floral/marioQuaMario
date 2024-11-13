package com.floralquafloral.bumping;

import com.floralquafloral.MarioPackets;
import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.bumping.handlers.BaselineBumpingHandler;
import com.floralquafloral.bumping.handlers.BumpingHandler;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import com.floralquafloral.registries.RegistryManager;
import com.floralquafloral.util.MarioSFX;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class BumpManager {
	public static final Map<BlockPos, BumpedBlockParticle> BUMPED_BLOCKS = new HashMap<>();
	public static final Set<BlockPos> HIDDEN_BLOCKS = new HashSet<>();
	private static final Set<BlockBumpingPlan> BLOCKS_TO_BUMP = new HashSet<>();

	private record BlockBumpingPlan(ClientWorld world, BlockPos pos, BlockState state, Direction direction) {}

	public static void registerPackets() {

	}
	public static void registerPacketsClient() {

	}

	private static BlockPos eyeAdjustmentParticlePos;
	@Nullable public static BumpedBlockParticle eyeAdjustmentParticle;

	private static final List<BumpingHandler> HANDLERS =
			RegistryManager.getEntrypoints("mario-bumping-handlers", BumpingHandler.class);
	private static final BaselineBumpingHandler BASELINE_HANDLER = new BaselineBumpingHandler();

	public static void registerEventListeners() {
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			HIDDEN_BLOCKS.clear();
			BUMPED_BLOCKS.clear();
		});

		ClientTickEvents.START_CLIENT_TICK.register(client -> {
			if(!BLOCKS_TO_BUMP.isEmpty()) {
				for(BlockBumpingPlan plan : BLOCKS_TO_BUMP) {
					visuallyBumpBlock(plan.world, plan.pos, plan.direction);
					MarioQuaMario.LOGGER.info("Pos1: {}, Pos2: {}, Equal: {}", plan.pos, eyeAdjustmentParticlePos, plan.pos.equals(eyeAdjustmentParticlePos));
					if(plan.pos.equals(eyeAdjustmentParticlePos)) {
						eyeAdjustmentParticle = BUMPED_BLOCKS.get(plan.pos);
					}
				}
				BLOCKS_TO_BUMP.clear();
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

	public static void bumpBlocks(MarioMainClientData data, ClientWorld world, Iterable<BlockPos> blocks, Direction direction, int baseStrength) {
		int bumpCount = 0, bumpAttemptCount = 0;
		int modifiedStrength = baseStrength + data.getPowerUp().BUMP_STRENGTH_MODIFIER + data.getCharacter().BUMP_STRENGTH_MODIFIER;
		Set<BlockSoundGroup> blockSoundGroups = new HashSet<>();
		BlockPos lastPos = null;
		for(BlockPos bumpPos : blocks) {
			bumpAttemptCount++;
			lastPos = new BlockPos(bumpPos);
			if(bumpBlockClient(
					data, world, lastPos,
					baseStrength, modifiedStrength, direction,
					blockSoundGroups)) {
				bumpCount++;
				// Network bump individually? :(
			}
		}

		if(bumpCount > 0) {
			for(BlockSoundGroup group : blockSoundGroups) {
				data.playSoundEvent(
						group.getPlaceSound(), SoundCategory.BLOCKS,
						data.getMario().getX(), data.getMario().getY() + data.getMario().getHeight(), data.getMario().getZ(),
						group.pitch * 0.8F, group.volume, Random.create().nextLong()
				);
			}
			data.playSoundEvent(
					MarioSFX.BUMP, SoundCategory.BLOCKS,
					data.getMario().getX(), data.getMario().getY() + data.getMario().getHeight(), data.getMario().getZ(),
					1.0F, 0.2F, Random.create().nextLong()
			);

			if(direction == Direction.DOWN && bumpCount == bumpAttemptCount) {
				MarioQuaMario.LOGGER.info("Bumped down with full success! Setting EAPP to {}", lastPos);
				eyeAdjustmentParticlePos = lastPos;
			}
		}
	}

	public static boolean bumpBlockClient(
			MarioClientSideData data, ClientWorld world, BlockPos pos,
			int baseStrength, int modifiedStrength, Direction direction,
			@Nullable Set<BlockSoundGroup> soundGroups
	) {
		BlockState blockState = world.getBlockState(pos);

		BumpingHandler.BumpLegality result = evaluateBumpLegality(blockState, world, pos, baseStrength, direction);

		if(result == BumpingHandler.BumpLegality.IGNORE) return false;

		bumpResponseClients(data, world, blockState, pos, baseStrength, modifiedStrength, direction);
		bumpResponseCommon(data, (data instanceof MarioMainClientData mainClientData) ? mainClientData : null,
				world, blockState, pos, baseStrength, modifiedStrength, direction);

		if (result == BumpingHandler.BumpLegality.SILENT_REACTION) return true;

		if (soundGroups == null) {
			visuallyBumpBlock(world, pos, direction);
			BlockSoundGroup group = blockState.getSoundGroup();
			data.playSoundEvent(
					group.getPlaceSound(), SoundCategory.BLOCKS,
					data.getMario().getX(), data.getMario().getY() + data.getMario().getHeight(), data.getMario().getZ(),
					group.pitch * 0.8F, group.volume, Random.create().nextLong()
			);
		} else {
			BLOCKS_TO_BUMP.add(new BlockBumpingPlan(world, pos, blockState, direction));
			soundGroups.add(blockState.getSoundGroup());
		}

		return true;
	}

	public void bumpBlockServer(
			MarioServerData data, ServerWorld world,BlockPos pos,
			int baseStrength, int modifiedStrength, Direction direction,
			boolean networkToMario
	) {
		bumpResponseCommon(data, data, world, world.getBlockState(pos), pos, baseStrength, modifiedStrength, direction);
//		MarioPackets.sendPacketToTrackersExclusive(data.getMario(), new );
	}

	public static @NotNull BumpingHandler.BumpLegality evaluateBumpLegality(
			BlockState state, BlockView world, BlockPos pos,
			int strength, Direction direction
	) {
		for(BumpingHandler handler : HANDLERS) {
			BumpingHandler.BumpLegality result = handler.evaluateBumpLegality(
					state, world, pos,
					strength, direction
			);
			if(result != null) return result;
		}
		return BASELINE_HANDLER.evaluateBumpLegality(state, world, pos, strength, direction);
	}

	public static void bumpResponseCommon(
			MarioData data, @Nullable MarioTravelData travelData, World world,
			BlockState state, BlockPos pos,
			int baseStrength, int modifiedStrength, Direction direction
	) {
		for(BumpingHandler handler : HANDLERS) {
			if(handler.bumpResponseCommon(data, travelData, world, state, pos, baseStrength, modifiedStrength, direction)) return;
		}
		BASELINE_HANDLER.bumpResponseCommon(data, travelData, world, state, pos, baseStrength, modifiedStrength, direction);
	}

	public static void bumpResponseClients(
			MarioClientSideData data, ClientWorld world,
			BlockState state, BlockPos pos,
			int baseStrength, int modifiedStrength, Direction direction
	) {
		for(BumpingHandler handler : HANDLERS) {
			if(handler.bumpResponseClients(data, world, state, pos, baseStrength, modifiedStrength, direction)) return;
		}
		BASELINE_HANDLER.bumpResponseClients(data, world, state, pos, baseStrength, modifiedStrength, direction);
	}
}
