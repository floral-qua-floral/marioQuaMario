package com.fqf.mario_qua_mario.packets;

import com.fqf.mario_qua_mario.MarioQuaMario;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

public class MarioPackets {
	public static void register() {
		MarioDataPackets.ActionTransitionS2CPayload.register();
		MarioDataPackets.AssignActionS2CPayload.register();

		MarioDataPackets.SetActionC2SPayload.register();

		MarioDataPackets.EmpowerRevertS2CPayload.register();
		MarioDataPackets.AssignPowerUpS2CPayload.register();

		MarioDataPackets.AssignCharacterS2CPayload.register();
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
}
