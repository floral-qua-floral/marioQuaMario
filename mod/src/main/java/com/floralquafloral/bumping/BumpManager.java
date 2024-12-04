package com.floralquafloral.bumping;

import com.floralquafloral.*;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import com.floralquafloral.BlockBumpResult;
import com.floralquafloral.mariodata.MarioClientSideData;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.*;

import static com.floralquafloral.MarioQuaMario.MOD_ID;

public abstract class BumpManager {

	public static final TagKey<Block> ALWAYS_REPEAT_BUMP = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "always_repeat_bump"));
	public static final TagKey<Block> NEVER_REPEAT_BUMP = TagKey.of(RegistryKeys.BLOCK, Identifier.of(MOD_ID, "never_repeat_bump"));

	public static final RegistryKey<DamageType> CEILING_BONK_DAMAGE =
			RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of(MOD_ID, "ceiling_bonk"));

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

	public static void registerEventListeners() {
		ServerTickEvents.END_SERVER_TICK.register((server) -> {
			Set<BlockBumpHandler.ForcedSignalSpot> removeSpots = null;
			Set<BlockBumpHandler.ForcedSignalSpot> copiedSet = new HashSet<>(BlockBumpHandler.FORCED_SIGNALS_DATA);
			for(BlockBumpHandler.ForcedSignalSpot spot : copiedSet) {
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
			if(blockState.isOf(Blocks.SLIME_BLOCK)) {
				if(marioTravelData != null)
					marioTravelData.getMario().setVelocity(marioTravelData.getMario().getVelocity().withAxis(
							direction.getAxis(), direction.getDirection().offset() * -1 * (1 + strength * 0.2)
					));
//					marioTravelData.getMario().setVelocity(new Vec3d(direction.getUnitVector()).multiply(-2));
				return BlockBumpResult.DISPLACE;
			}
			return BlockBumpResult.PASS;
		});
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

	record BumpC2SPayload(Set<BlockPos> positions, Direction direction, int strength) implements CustomPayload {
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
				BumpManagerClient.bumpBlocksClient(
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
