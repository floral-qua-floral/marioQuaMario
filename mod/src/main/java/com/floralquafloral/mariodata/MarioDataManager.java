package com.floralquafloral.mariodata;

import com.floralquafloral.MarioPackets;
import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.moveable.MarioMainClientData;
import com.floralquafloral.mariodata.moveable.MarioServerData;
import com.floralquafloral.registries.RegistryManager;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MarioDataManager {
	private static final Map<PlayerEntity, MarioPlayerData> SERVER_PLAYERS_DATA = new HashMap<>();
	private static final Map<PlayerEntity, MarioPlayerData> CLIENT_PLAYERS_DATA = new HashMap<>();
	public static boolean useCharacterStats = true;

	public static void registerEventListeners() {
		ServerLifecycleEvents.SERVER_STARTED.register((server) -> {
			useCharacterStats = server.getGameRules().getBoolean(MarioQuaMario.USE_CHARACTER_STATS);
			SERVER_PLAYERS_DATA.clear();
		});

		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
			MarioQuaMario.LOGGER.info("Player respawned!"
					+ "\nOld: " + oldPlayer
					+ "\nNew: " + newPlayer
					+ "\nAlive: " + alive
			);

			MarioPlayerData data = getMarioData(oldPlayer);
			data.setMario(newPlayer);
			data.setEnabledInternal(data.isEnabled());
			SERVER_PLAYERS_DATA.put(newPlayer, data);
			data.setPowerUp(Objects.requireNonNull(RegistryManager.POWER_UPS.get(Identifier.of(MarioQuaMario.MOD_ID, "super"))));
			MarioDataPackets.sendAllData(newPlayer, newPlayer);
		});



		ServerTickEvents.START_SERVER_TICK.register((server) -> {
			PlayerEntity removeMe = null;
			Set<Map.Entry<PlayerEntity, MarioPlayerData>> entrySet = SERVER_PLAYERS_DATA.entrySet();
			for(Map.Entry<PlayerEntity, MarioPlayerData> entry : entrySet) {
				ServerPlayerEntity player = (ServerPlayerEntity) entry.getKey();
				if(player.isDisconnected()) {
					MarioQuaMario.LOGGER.info("Removing player: {}", player);
					removeMe = player;
					continue;
				}

				entry.getValue().tick();
			}

			// This approach means we can only remove one player per tick. I don't care!!!!!!!!!!! Bite me!
			if(removeMe != null) {
				SERVER_PLAYERS_DATA.remove(removeMe);
			}
		});
	}

	public static void registerClientEventListeners() {
		ClientTickEvents.START_CLIENT_TICK.register((client) -> {
			for(Map.Entry<PlayerEntity, MarioPlayerData> entry : CLIENT_PLAYERS_DATA.entrySet()) {
				if(!entry.getKey().isRemoved()) entry.getValue().tick();
			}
		});

		ClientEntityEvents.ENTITY_LOAD.register((entity, world) -> {
			if(entity instanceof PlayerEntity clientPlayer) {
				MarioMainClientData data = MarioMainClientData.getInstance();
				if(data == null) return;
				CLIENT_PLAYERS_DATA.remove(clientPlayer);
				CLIENT_PLAYERS_DATA.remove(data.getMario());
				data.setMario(clientPlayer);
				data.setEnabledInternal(data.isEnabled());
				CLIENT_PLAYERS_DATA.put(clientPlayer, data);
			}
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> CLIENT_PLAYERS_DATA.clear());
	}

	public static void wipePlayerData() {
//		SERVER_PLAYERS_DATA.clear();
//		CLIENT_PLAYERS_DATA.clear();
	}

	public static MarioPlayerData getMarioData(PlayerEntity mario) {
		boolean isClient = mario.getWorld().isClient;
		final Map<PlayerEntity, MarioPlayerData> RELEVANT_MAP = isClient ? CLIENT_PLAYERS_DATA : SERVER_PLAYERS_DATA;
		MarioPlayerData playerData = RELEVANT_MAP.get(mario);

		if(playerData == null) {
			if(mario.isMainPlayer())
				playerData = new MarioMainClientData(mario);
			else if(isClient)
				playerData = new MarioOtherClientData(mario);
			else
				playerData = new MarioServerData((ServerPlayerEntity) mario);

			RELEVANT_MAP.put(mario, playerData);
		}

		return playerData;
	}
	public static MarioPlayerData getMarioData(ServerPlayNetworking.Context context, int playerID) {
		return getMarioData(MarioPackets.getPlayerFromInt(context, playerID));
	}
	public static MarioPlayerData getMarioData(ClientPlayNetworking.Context context, int playerID) {
		return getMarioData(MarioPackets.getPlayerFromInt(context, playerID));
	}
	public static MarioPlayerData getMarioData(Object object) {
		return getMarioData((PlayerEntity) object);
	}
}
