package com.floralquafloral.mariodata;

import com.floralquafloral.MarioPackets;
import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.mixin.PlayerEntityMixin;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MarioDataManager {
	private static final Map<PlayerEntity, MarioData> SERVER_PLAYERS_DATA = new HashMap<>();
	private static final Map<PlayerEntity, MarioData> CLIENT_PLAYERS_DATA = new HashMap<>();

	public static void registerEventListeners() {
		ServerLifecycleEvents.SERVER_STARTING.register((server) -> wipePlayerData());

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			MarioQuaMario.LOGGER.info("Player respawned!"
					+ "\nOld: " + oldPlayer
					+ "\nNew: " + newPlayer
					+ "\nAlive: " + alive
			);

			MarioData data = getMarioData(oldPlayer);
			((MarioPlayerData) data).setMario(newPlayer);
			SERVER_PLAYERS_DATA.put(newPlayer, data);
			MarioDataPackets.sendAllData(newPlayer, newPlayer);
		});



		ServerTickEvents.START_SERVER_TICK.register((server) -> {
			PlayerEntity removeMe = null;
			Set<Map.Entry<PlayerEntity, MarioData>> entrySet = SERVER_PLAYERS_DATA.entrySet();
			for(Map.Entry<PlayerEntity, MarioData> entry : entrySet) {
				ServerPlayerEntity player = (ServerPlayerEntity) entry.getKey();
				if(player.isDisconnected()) {
					MarioQuaMario.LOGGER.info("Removing player: {}", player);
					removeMe = player;
					continue;
				}

				((MarioPlayerData) entry.getValue()).tick();
			}

			// This approach means we can only remove one player per tick. I don't care!!!!!!!!!!! Bite me!
			if(removeMe != null) {
				SERVER_PLAYERS_DATA.remove(removeMe);
			}
		});

		ClientTickEvents.START_CLIENT_TICK.register((client) -> {
			for(Map.Entry<PlayerEntity, MarioData> entry : CLIENT_PLAYERS_DATA.entrySet()) {
				if(!entry.getKey().isRemoved()) ((MarioPlayerData) entry.getValue()).tick();
			}
		});

		ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if(entity instanceof ClientPlayerEntity clientPlayer) {
				MarioClientData data = MarioClientData.getInstance();
				if(data == null) return;
				CLIENT_PLAYERS_DATA.remove(clientPlayer);
				CLIENT_PLAYERS_DATA.remove(data.getMario());
				data.setMario(clientPlayer);
				CLIENT_PLAYERS_DATA.put(clientPlayer, data);
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> wipePlayerData());

//		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
//
//		});


	}

	public static void wipePlayerData() {
		SERVER_PLAYERS_DATA.clear();
		CLIENT_PLAYERS_DATA.clear();
	}

	public static MarioData getMarioData(PlayerEntity mario) {
		boolean isClient = mario.getWorld().isClient;
		final Map<PlayerEntity, MarioData> RELEVANT_MAP = isClient ? CLIENT_PLAYERS_DATA : SERVER_PLAYERS_DATA;
		MarioData playerData = RELEVANT_MAP.get(mario);

		if(playerData == null) {
			if(mario.isMainPlayer() && mario instanceof ClientPlayerEntity marioClient) {
				playerData = new MarioClientData(marioClient);
//				playerData = MarioClientData.getInstance();
//				if(playerData == null) playerData = new MarioClientData(marioClient);
//				else ((MarioClientData) playerData).setMario(mario);
			}
			else// if(isClient)
				playerData = new MarioPlayerData(mario);
//			else
//				playerData = new MarioServerPlayerData((ServerPlayerEntity) mario);
			RELEVANT_MAP.put(mario, playerData);
		}

		return playerData;
	}
	public static MarioData getMarioData(ServerPlayNetworking.Context context, int playerID) {
		return getMarioData(MarioPackets.getPlayerFromInt(context, playerID));
	}
	public static MarioData getMarioData(ClientPlayNetworking.Context context, int playerID) {
		return getMarioData(MarioPackets.getPlayerFromInt(context, playerID));
	}
	public static MarioData getMarioData(Object object) {
		return getMarioData((PlayerEntity) object);
	}
}
