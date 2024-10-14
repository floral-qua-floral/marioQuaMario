package com.floralquafloral.mariodata;

import com.floralquafloral.MarioPackets;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.HashMap;
import java.util.Map;

public class MarioDataManager {
	private static final Map<PlayerEntity, MarioData> SERVER_PLAYERS_DATA = new HashMap<>();
	private static final Map<PlayerEntity, MarioData> CLIENT_PLAYERS_DATA = new HashMap<>();

	public static void wipePlayerData() {
		SERVER_PLAYERS_DATA.clear();
		CLIENT_PLAYERS_DATA.clear();
	}

	public static MarioData getMarioData(PlayerEntity mario) {
		final Map<PlayerEntity, MarioData> RELEVANT_MAP = mario.getWorld().isClient ? CLIENT_PLAYERS_DATA : SERVER_PLAYERS_DATA;
		MarioData playerData = RELEVANT_MAP.get(mario);

		if(playerData == null) {
			if(mario.isMainPlayer() && mario instanceof ClientPlayerEntity marioClient)
				playerData = new MarioClientData(marioClient);
			else
				playerData = new MarioPlayerData(mario);
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


}
