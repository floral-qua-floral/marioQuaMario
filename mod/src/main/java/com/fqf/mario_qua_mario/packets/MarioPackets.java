package com.fqf.mario_qua_mario.packets;

import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import com.fqf.mario_qua_mario.registries.actions.AbstractParsedAction;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;

public class MarioPackets {
	public static void register() {
		MarioDataPackets.SetActionS2CPayload.register();

		MarioDataPackets.SetActionC2SPayload.register();
	}

	public static void sendToTrackers(ServerPlayerEntity mario, CustomPayload packet, boolean includeMario) {
		if(includeMario) ServerPlayNetworking.send(mario, packet);
		for(ServerPlayerEntity player : PlayerLookup.tracking(mario)) {
			if(!player.equals(mario)) ServerPlayNetworking.send(player, packet);
		}
	}
}
