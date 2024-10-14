package com.floralquafloral;

import com.floralquafloral.mariodata.MarioDataPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

public class MarioPackets {
	public static void registerCommon() {
		MarioDataPackets.registerCommon();
	}
	public static void registerClient() {
		MarioDataPackets.registerClient();
	}

	public static PlayerEntity getPlayerFromInt(ClientPlayNetworking.Context context, int playerID) {
		return (PlayerEntity) context.player().getWorld().getEntityById(playerID);
	}
	public static ServerPlayerEntity getPlayerFromInt(ServerPlayNetworking.Context context, int playerID) {
		return (ServerPlayerEntity) context.player().getServerWorld().getEntityById(playerID);
	}

	public static void sendPacketToTrackers(ServerPlayerEntity mario, boolean includeMario, CustomPayload packet) {
		Collection<ServerPlayerEntity> sendToPlayers = PlayerLookup.tracking(mario);
		if(includeMario) {
			for(ServerPlayerEntity player : sendToPlayers)
				ServerPlayNetworking.send(player, packet);
			if(!sendToPlayers.contains(mario))
				ServerPlayNetworking.send(mario, packet);
		}
		else
			for(ServerPlayerEntity player : sendToPlayers)
				if(player != mario) ServerPlayNetworking.send(player, packet);
	}
}
