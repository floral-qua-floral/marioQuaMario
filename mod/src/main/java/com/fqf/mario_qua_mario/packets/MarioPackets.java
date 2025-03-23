package com.fqf.mario_qua_mario.packets;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.interfaces.StompResult;
import com.fqf.mario_qua_mario.registries.ParsedStompType;
import com.fqf.mario_qua_mario.registries.RegistryManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class MarioPackets {
	public static void register() {
		MarioDataPackets.DisableMarioS2CPayload.register();

		MarioDataPackets.ActionTransitionS2CPayload.register();
		MarioDataPackets.AssignActionS2CPayload.register();

		MarioDataPackets.SetActionC2SPayload.register();

		MarioDataPackets.EmpowerRevertS2CPayload.register();
		MarioDataPackets.AssignPowerUpS2CPayload.register();

		MarioDataPackets.AssignCharacterS2CPayload.register();

		MarioDataPackets.SyncMarioDataS2CPayload.register();

		SyncUseCharacterStatsS2CPayload.register();

		StompS2CPayload.register();

		MarioAttackInterceptionPackets.MissedAttackInterceptedC2SPayload.register();
		MarioAttackInterceptionPackets.EntityAttackInterceptedC2SPayload.register();
		MarioAttackInterceptionPackets.BlockAttackInterceptedC2SPayload.register();

		MarioAttackInterceptionPackets.MissedAttackInterceptedS2CPayload.register();
		MarioAttackInterceptionPackets.EntityAttackInterceptedS2CPayload.register();
		MarioAttackInterceptionPackets.BlockAttackInterceptedS2CPayload.register();
	}

	public static void syncUseCharacterStatsS2C(MinecraftServer server, boolean shouldUse) {
		CustomPayload packet = new SyncUseCharacterStatsS2CPayload(shouldUse);
		for(ServerPlayerEntity player : PlayerLookup.all(server)) {
			ServerPlayNetworking.send(player, packet);
		}
	}

	public static void syncUseCharacterStatsS2C(ServerPlayerEntity player, boolean shouldUse) {
		ServerPlayNetworking.send(player, new SyncUseCharacterStatsS2CPayload(shouldUse));
	}

	public static void stompS2C(ServerPlayerEntity mario, ParsedStompType stompType, Entity stompedEntity, StompResult.ExecutableResult result, boolean affectMario) {
		sendToTrackers(mario, new StompS2CPayload(
				mario.getId(),
				RegistryManager.STOMP_TYPES.getRawIdOrThrow(stompType),
				stompedEntity.getId(),
				result.ordinal(),
				affectMario,
				mario.getRandom().nextLong()
		), true);
	}

	public static void sendToTrackers(ServerPlayerEntity mario, CustomPayload packet, boolean includeMario) {
		if(includeMario) ServerPlayNetworking.send(mario, packet);
		for(ServerPlayerEntity player : PlayerLookup.tracking(mario)) {
			if(!player.equals(mario)) ServerPlayNetworking.send(player, packet);
		}
	}

	public static <T extends CustomPayload> CustomPayload.Id<T> makeID(String path) {
		return new CustomPayload.Id<>(MarioQuaMario.makeID(path));
	}

	protected record SyncUseCharacterStatsS2CPayload(boolean shouldUse) implements CustomPayload {
		public static final Id<SyncUseCharacterStatsS2CPayload> ID = MarioPackets.makeID("sync_use_character_stats_s2c");
		public static final PacketCodec<RegistryByteBuf, SyncUseCharacterStatsS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.BOOL, SyncUseCharacterStatsS2CPayload::shouldUse,
				SyncUseCharacterStatsS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}

	protected record StompS2CPayload(int marioID, int stompTypeID, int stompedEntityID, int stompResultIndex, boolean affectMario, long seed) implements CustomPayload {
		public static final Id<StompS2CPayload> ID = MarioPackets.makeID("stomp_s2c");
		public static final PacketCodec<RegistryByteBuf, StompS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.INTEGER, StompS2CPayload::marioID,
				PacketCodecs.INTEGER, StompS2CPayload::stompTypeID,
				PacketCodecs.INTEGER, StompS2CPayload::stompedEntityID,
				PacketCodecs.INTEGER, StompS2CPayload::stompResultIndex,
				PacketCodecs.BOOL, StompS2CPayload::affectMario,
				PacketCodecs.VAR_LONG, StompS2CPayload::seed,
				StompS2CPayload::new
		);

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}
}
