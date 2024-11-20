package com.floralquafloral.bumping;

import com.floralquafloral.*;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import com.floralquafloral.BlockBumpResult;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.util.MarioSFX;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.floralquafloral.MarioQuaMario.MOD_ID;

public abstract class BumpManager {
	public static final Map<BlockPos, BumpedBlockParticle> BUMPED_BLOCKS = new HashMap<>();
	public static final Set<BlockPos> HIDDEN_BLOCKS = new HashSet<>();
	private static final Set<BlockBumpingPlan> BLOCKS_TO_DISPLACE = new HashSet<>();

	public static final TagKey<Block> ALWAYS_REPEAT_BUMP = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "always_repeat_bump"));
	public static final TagKey<Block> NEVER_REPEAT_BUMP = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "never_repeat_bump"));

	private record BlockBumpingPlan(ClientWorld world, BlockPos pos, BlockState state, Direction direction) {}

	public static void registerPackets() {
		BumpC2SPayload.register();
		BumpC2SPayload.registerReceiver();

		BumpS2CPayload.register();
		AllowRepeatBumpS2CPayload.register();
	}
	public static void registerPacketsClient() {
		BumpS2CPayload.registerReceiver();
		AllowRepeatBumpS2CPayload.registerReceiver();
	}

	private static BlockPos eyeAdjustmentParticlePos;
	@Nullable public static BumpedBlockParticle eyeAdjustmentParticle;

	public static void registerEventListeners() {
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

		ServerTickEvents.END_SERVER_TICK.register((server) -> {
			Set<BlockBumpHandler.ForcedSignalSpot> removeSpots = null;
			for(BlockBumpHandler.ForcedSignalSpot spot : BlockBumpHandler.FORCED_SIGNALS_DATA) {
				if(spot.delay-- <= 0) {
					BlockBumpHandler.FORCED_SIGNALS.remove(spot.POSITION);
					spot.WORLD.updateNeighbor(spot.POSITION, spot.WORLD.getBlockState(spot.POSITION).getBlock(), spot.POSITION);
					if(removeSpots == null) removeSpots = new HashSet<>();
					removeSpots.add(spot);
				}
			}
			if(removeSpots != null) for(BlockBumpHandler.ForcedSignalSpot spot : removeSpots) {
				BlockBumpHandler.FORCED_SIGNALS_DATA.remove(spot);
			}
		});

		BlockBumpHandler.EVENT.register((
				marioData, marioClientData, marioTravelData,
				world, blockPos, blockState,
				strength, modifier, direction
		) -> {
			MarioQuaMario.LOGGER.info("Bump listener!!");
			return BlockBumpResult.PASS;
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

	public static void attemptBumpBlocks(
			MarioMainClientData data, ClientWorld world,
			Iterable<BlockPos> blocks, Direction direction,
			int baseStrength
	) {
		data.canRepeatPound = false;
		Set<BlockPos> fullBlocksSet = new HashSet<>();
		for(BlockPos pos : blocks) fullBlocksSet.add(new BlockPos(pos));

		Set<BlockPos> prunedBlocksSet = bumpBlocksClient(data, data, world, fullBlocksSet, baseStrength, direction);

		if(!prunedBlocksSet.isEmpty()) {
			ClientPlayNetworking.send(new BumpC2SPayload(prunedBlocksSet, direction, baseStrength));
			if (fullBlocksSet.size() == prunedBlocksSet.size() && direction == Direction.DOWN) {
				// this will cause eye height adjustment even if some blocks returned CANCEL_NETWORKED :(
				eyeAdjustmentParticlePos = prunedBlocksSet.stream().findFirst().get();
			}
		}
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

	public static void bumpBlocksServer(
			MarioServerData marioServerData,
			ServerWorld world, Set<BlockPos> positions,
			int strength, Direction direction, boolean networkToBumper
	) {
		boolean canRepeatBump = false;
		int modifier = marioServerData.getBumpStrengthModifier();
		Set<BlockPos> bumpedPositions = new HashSet<>();

		for(BlockPos pos : positions) {
			BlockState state = world.getBlockState(pos);

			BlockBumpResult result = BlockBumpHandler.processBumpResult(
					marioServerData, null, marioServerData,
					world, pos, state,
					strength, modifier, direction
			);

			if(
					result == BlockBumpResult.DISPLACE
					&& (state.isIn(ALWAYS_REPEAT_BUMP) || !state.equals(world.getBlockState(pos)))
					&& !state.isIn(NEVER_REPEAT_BUMP)
			) canRepeatBump = true;
			if(result != BlockBumpResult.CANCEL) bumpedPositions.add(pos);
		}

		if(canRepeatBump) ServerPlayNetworking.send(marioServerData.getMario(), new AllowRepeatBumpS2CPayload());

		if(!bumpedPositions.isEmpty()) {
			BumpS2CPayload payload = new BumpS2CPayload(marioServerData.getMario().getId(), bumpedPositions, direction, strength);
			MarioPackets.sendPacketToTrackersExclusive(marioServerData.getMario(), payload);
			if(networkToBumper) ServerPlayNetworking.send(marioServerData.getMario(), payload);
		}
	}

	private record BumpC2SPayload(Set<BlockPos> positions, Direction direction, int strength) implements CustomPayload {
		public static final CustomPayload.Id<BumpC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(MarioQuaMario.MOD_ID, "bump_c2s"));
		public static final PacketCodec<RegistryByteBuf, BumpC2SPayload> CODEC = PacketCodec.tuple(
				BlockPos.PACKET_CODEC.collect(PacketCodecs.toCollection(HashSet::new)), BumpC2SPayload::positions,
				Direction.PACKET_CODEC, BumpC2SPayload::direction,
				PacketCodecs.INTEGER, BumpC2SPayload::strength,
				BumpC2SPayload::new
		);
		public static void registerReceiver() {
			ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
				MarioServerData data = (MarioServerData) MarioDataManager.getMarioData(context.player());
				bumpBlocksServer(data, context.player().getServerWorld(), payload.positions, payload.strength, payload.direction, false);
			});
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playC2S().register(ID, CODEC);
		}
	}

	private record BumpS2CPayload(int player, Set<BlockPos> positions, Direction direction, int strength) implements CustomPayload {
		public static final CustomPayload.Id<BumpS2CPayload> ID = new CustomPayload.Id<>(Identifier.of(MarioQuaMario.MOD_ID, "bump_s2c"));
		public static final PacketCodec<RegistryByteBuf, BumpS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, BumpS2CPayload::player,
				BlockPos.PACKET_CODEC.collect(PacketCodecs.toCollection(HashSet::new)), BumpS2CPayload::positions,
				Direction.PACKET_CODEC, BumpS2CPayload::direction,
				PacketCodecs.INTEGER, BumpS2CPayload::strength,
				BumpS2CPayload::new
		);
		public static void registerReceiver() {
			ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
				MarioClientSideData data = (MarioClientSideData) MarioDataManager.getMarioData(context.player());
				bumpBlocksClient(
						data, null,
						context.player().clientWorld, payload.positions,
						payload.strength, payload.direction
				);
			});
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	private record AllowRepeatBumpS2CPayload() implements CustomPayload {
		public static final CustomPayload.Id<AllowRepeatBumpS2CPayload> ID = new CustomPayload.Id<>(Identifier.of(MarioQuaMario.MOD_ID, "allow_repeat_bump"));
		public static final PacketCodec<RegistryByteBuf, AllowRepeatBumpS2CPayload> CODEC = PacketCodec.unit(new AllowRepeatBumpS2CPayload());
		public static void registerReceiver() {
			ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
				MarioMainClientData data = MarioMainClientData.getInstance();
				if(data != null) data.canRepeatPound = true;
			});
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}
}
