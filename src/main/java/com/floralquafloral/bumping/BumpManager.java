package com.floralquafloral.bumping;

import com.floralquafloral.MarioPackets;
import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.bumping.handlers.BaselineBumpingHandler;
import com.floralquafloral.bumping.handlers.BumpingHandler;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import com.floralquafloral.mariodata.moveable.MarioTravelData;
import com.floralquafloral.registries.RegistryManager;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
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

import static com.floralquafloral.MarioQuaMario.MOD_ID;

public abstract class BumpManager {
	public static final Map<BlockPos, BumpedBlockParticle> BUMPED_BLOCKS = new HashMap<>();
	public static final Set<BlockPos> HIDDEN_BLOCKS = new HashSet<>();
	private static final Set<BlockBumpingPlan> BLOCKS_TO_BUMP = new HashSet<>();

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

		ServerTickEvents.END_SERVER_TICK.register((server) -> {
			Set<BaselineBumpingHandler.ForcedSignalSpot> removeSpots = null;
			for(BaselineBumpingHandler.ForcedSignalSpot spot : BaselineBumpingHandler.FORCED_SIGNALS_DATA) {
				if(spot.delay-- <= 0) {
					BaselineBumpingHandler.FORCED_SIGNALS.remove(spot.POSITION);
					spot.WORLD.updateNeighbor(spot.POSITION, spot.WORLD.getBlockState(spot.POSITION).getBlock(), spot.POSITION);
					if(removeSpots == null) removeSpots = new HashSet<>();
					removeSpots.add(spot);
				}
			}
			if(removeSpots != null) for(BaselineBumpingHandler.ForcedSignalSpot spot : removeSpots) {
				BaselineBumpingHandler.FORCED_SIGNALS_DATA.remove(spot);
			}
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
		data.getTimers().canRepeatPound = false;
		for(BlockPos bumpPos : blocks) {
			bumpAttemptCount++;
			lastPos = new BlockPos(bumpPos);
			if(bumpBlockClient(
					data, world, lastPos,
					baseStrength, modifiedStrength, direction,
					false,
					blockSoundGroups)) {
				bumpCount++;
				ClientPlayNetworking.send(new BumpC2SPayload(lastPos, direction, baseStrength));
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
				eyeAdjustmentParticlePos = lastPos;
			}
		}
	}

	public static boolean bumpBlockClient(
			MarioClientSideData data, ClientWorld world, BlockPos pos,
			int baseStrength, int modifiedStrength, Direction direction,
			boolean force, @Nullable Set<BlockSoundGroup> soundGroups
	) {
		BlockState blockState = world.getBlockState(pos);

		BumpingHandler.BumpLegality result = evaluateBumpLegality(blockState, world, pos, Math.max(baseStrength, modifiedStrength), direction);

		if(!force && result == BumpingHandler.BumpLegality.IGNORE) return false;

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

	public static void bumpBlockServer(
			MarioServerData data, ServerWorld world, BlockPos pos,
			int baseStrength, int modifiedStrength, Direction direction,
			boolean force, boolean networkToMario
	) {
		BlockState blockState = world.getBlockState(pos);
		if(!force && evaluateBumpLegality(blockState, world, pos, Math.max(baseStrength, modifiedStrength), direction)
				== BumpingHandler.BumpLegality.IGNORE) return;

		bumpResponseCommon(data, data, world, blockState, pos, baseStrength, modifiedStrength, direction);

		BumpS2CPayload packet = new BumpS2CPayload(data.getMario().getId(), pos, direction, baseStrength);
		MarioPackets.sendPacketToTrackersExclusive(data.getMario(), packet);
		if(networkToMario) ServerPlayNetworking.send(data.getMario(), packet);
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
		response: {
			for(BumpingHandler handler : HANDLERS) {
				if(handler.bumpResponseCommon(data, travelData, world, state, pos, baseStrength, modifiedStrength, direction))
					break response;
			}
			BASELINE_HANDLER.bumpResponseCommon(data, travelData, world, state, pos, baseStrength, modifiedStrength, direction);
		}
		if(!state.isIn(NEVER_REPEAT_BUMP) && (state.isIn(ALWAYS_REPEAT_BUMP) || !state.equals(world.getBlockState(pos)))) {
			if(data.getMario() instanceof ServerPlayerEntity marioServer)
				ServerPlayNetworking.send(marioServer, new AllowRepeatBumpS2CPayload());
			else if(data.getMario().isMainPlayer())
				((MarioMainClientData) data).getTimers().canRepeatPound = true; // is this even possible??
		}
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

	private record BumpC2SPayload(BlockPos pos, Direction direction, int strength) implements CustomPayload {
		public static final CustomPayload.Id<BumpC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(MarioQuaMario.MOD_ID, "bump_c2s"));
		public static final PacketCodec<RegistryByteBuf, BumpC2SPayload> CODEC = PacketCodec.tuple(
				BlockPos.PACKET_CODEC, BumpC2SPayload::pos,
				Direction.PACKET_CODEC, BumpC2SPayload::direction,
				PacketCodecs.INTEGER, BumpC2SPayload::strength,
				BumpC2SPayload::new
		);
		public static void registerReceiver() {
			ServerPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
				MarioServerData data = (MarioServerData) MarioDataManager.getMarioData(context.player());
				bumpBlockServer(
						data, context.player().getServerWorld(), payload.pos,
						payload.strength,
						payload.strength + data.getCharacter().BUMP_STRENGTH_MODIFIER + data.getPowerUp().BUMP_STRENGTH_MODIFIER,
						payload.direction,
						false, false
				);
			});
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playC2S().register(ID, CODEC);
		}
	}

	private record BumpS2CPayload(int player, BlockPos pos, Direction direction, int strength) implements CustomPayload {
		public static final CustomPayload.Id<BumpS2CPayload> ID = new CustomPayload.Id<>(Identifier.of(MarioQuaMario.MOD_ID, "bump_s2c"));
		public static final PacketCodec<RegistryByteBuf, BumpS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, BumpS2CPayload::player,
				BlockPos.PACKET_CODEC, BumpS2CPayload::pos,
				Direction.PACKET_CODEC, BumpS2CPayload::direction,
				PacketCodecs.INTEGER, BumpS2CPayload::strength,
				BumpS2CPayload::new
		);
		public static void registerReceiver() {
			ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) -> {
				MarioClientSideData data = (MarioClientSideData) MarioDataManager.getMarioData(context.player());
				bumpBlockClient(
						data, context.player().clientWorld, payload.pos,
						payload.strength,
						payload.strength + data.getCharacter().BUMP_STRENGTH_MODIFIER + data.getPowerUp().BUMP_STRENGTH_MODIFIER,
						payload.direction,
						true,
						null
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
				if(data != null) data.getTimers().canRepeatPound = true;
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
