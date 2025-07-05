package com.fqf.mario_qua_mario.packets;

import com.fqf.mario_qua_mario.bapping.BlockBappingUtil;
import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import com.fqf.mario_qua_mario_api.interfaces.BapResult;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import javax.annotation.Nullable;

public class MarioBappingPackets {
	public static void bapS2C(
			ServerWorld world, BlockPos pos,
			Direction direction, int strength, BapResult result,
			Entity bapper, boolean networkToBapper
	) {
		CustomPayload payload = new BapBlockS2CPayload(pos, direction.ordinal(), strength, result.ordinal(), bapper.getId());
		for(ServerPlayerEntity player : PlayerLookup.tracking(world, pos)) {
			if(player != bapper || networkToBapper)
				ServerPlayNetworking.send(player, payload);
		}
	}

	protected record BapBlockC2SPayload(BlockPos pos, int direction, int action) implements CustomPayload {
		public static final Id<BapBlockC2SPayload> ID = MarioPackets.makeID("bap_block_c2s");
		public static final PacketCodec<RegistryByteBuf, BapBlockC2SPayload> CODEC = PacketCodec.tuple(
				BlockPos.PACKET_CODEC, BapBlockC2SPayload::pos,
				PacketCodecs.INTEGER, BapBlockC2SPayload::direction,
				PacketCodecs.INTEGER, BapBlockC2SPayload::action,
				BapBlockC2SPayload::new
		);

		public static void receive(BapBlockC2SPayload payload, ServerPlayNetworking.Context context) {
			MarioServerPlayerData data = context.player().mqm$getMarioData();
			Direction direction = Direction.values()[payload.direction];

			AbstractParsedAction bappingAction = RegistryManager.ACTIONS.getOrThrow(payload.action);
			if(data.recentlyInAction(bappingAction))
				BlockBappingUtil.attemptBap(
						data,
						context.player().getServerWorld(),
						payload.pos,
						direction,
						data.getBapStrength(bappingAction, direction)
				);
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playC2S().register(ID, CODEC);
			ServerPlayNetworking.registerGlobalReceiver(ID, BapBlockC2SPayload::receive);
		}
	}

	protected record BapBlockS2CPayload(BlockPos pos, int direction, int strength, int result, int bapperID) implements CustomPayload {
		public static final Id<BapBlockS2CPayload> ID = MarioPackets.makeID("bap_block_s2c");
		public static final PacketCodec<RegistryByteBuf, BapBlockS2CPayload> CODEC = PacketCodec.tuple(
				BlockPos.PACKET_CODEC, BapBlockS2CPayload::pos,
				PacketCodecs.INTEGER, BapBlockS2CPayload::direction,
				PacketCodecs.INTEGER, BapBlockS2CPayload::strength,
				PacketCodecs.INTEGER, BapBlockS2CPayload::result,
				PacketCodecs.INTEGER, BapBlockS2CPayload::bapperID,
				BapBlockS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}
}
