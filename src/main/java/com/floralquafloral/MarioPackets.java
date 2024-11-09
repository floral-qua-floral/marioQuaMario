package com.floralquafloral;

import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.mariodata.MarioDataPackets;
import com.floralquafloral.registries.stomp.StompHandler;
import com.floralquafloral.util.JumpSoundPlayer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Collection;

public class MarioPackets {
	public static void registerCommon() {
		SyncUseCharacterStatsS2CPayload.register();
		MarioDataPackets.registerCommon();
		StompHandler.registerPackets();
//		VoiceLine.registerPackets();
		JumpSoundPlayer.registerPackets();

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			MarioQuaMario.LOGGER.info("");
		});
	}
	public static void registerClient() {
		SyncUseCharacterStatsS2CPayload.registerReceiver();
		MarioDataPackets.registerClient();
		StompHandler.registerPacketsClient();
//		VoiceLine.registerPacketsClient();
		JumpSoundPlayer.registerPacketsClient();
	}

	public static PlayerEntity getPlayerFromInt(ClientPlayNetworking.Context context, int playerID) {
		return (PlayerEntity) context.player().getWorld().getEntityById(playerID);
	}
	public static ServerPlayerEntity getPlayerFromInt(ServerPlayNetworking.Context context, int playerID) {
		return (ServerPlayerEntity) context.player().getServerWorld().getEntityById(playerID);
	}

	public static void sendPacketToTrackers(ServerPlayerEntity mario, CustomPayload packet) {
		Collection<ServerPlayerEntity> sendToPlayers = PlayerLookup.tracking(mario);
		for(ServerPlayerEntity player : sendToPlayers)
			ServerPlayNetworking.send(player, packet);
		if(!sendToPlayers.contains(mario))
			ServerPlayNetworking.send(mario, packet);
	}

	public static void sendPacketToTrackersExclusive(ServerPlayerEntity mario, CustomPayload packet) {
		Collection<ServerPlayerEntity> sendToPlayers = PlayerLookup.tracking(mario);
		for(ServerPlayerEntity player : sendToPlayers)
			if(player != mario) ServerPlayNetworking.send(player, packet);
	}

	public record SyncUseCharacterStatsS2CPayload(boolean useCharacterStats) implements CustomPayload {
		public static final Id<SyncUseCharacterStatsS2CPayload> ID = new Id<>(Identifier.of(MarioQuaMario.MOD_ID, "sync_use_character_stats"));
		public static final PacketCodec<RegistryByteBuf, SyncUseCharacterStatsS2CPayload> CODEC = PacketCodec.tuple(
				PacketCodecs.BOOL, SyncUseCharacterStatsS2CPayload::useCharacterStats,
				SyncUseCharacterStatsS2CPayload::new
		);
		public static void registerReceiver() {
			ClientPlayNetworking.registerGlobalReceiver(ID, (payload, context) ->
					MarioDataManager.useCharacterStats = payload.useCharacterStats);
		}

		@Override public Id<? extends CustomPayload> getId() {
			return ID;
		}
		public static void register() {
			PayloadTypeRegistry.playS2C().register(ID, CODEC);
		}
	}
}
