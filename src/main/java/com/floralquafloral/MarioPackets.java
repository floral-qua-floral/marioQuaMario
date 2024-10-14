package com.floralquafloral;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

public class MarioPackets {

	public static void registerCommon() {

	}
	public static void registerClient() {

	}

	public static PlayerEntity getPlayerFromInt(ClientPlayNetworking.Context context, int playerID) {
		return (PlayerEntity) context.player().getWorld().getEntityById(playerID);
	}
	public static ServerPlayerEntity getPlayerFromInt(ServerPlayNetworking.Context context, int playerID) {
		return (ServerPlayerEntity) context.player().getServerWorld().getEntityById(playerID);
	}
}
