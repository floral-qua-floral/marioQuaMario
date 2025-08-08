package com.fqf.mario_qua_mario.compat;

import com.moulberry.flashback.Flashback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.packet.CustomPayload;

class FlashbackCompatUnsafe {
	static void saveS2CPacketToReplay(CustomPayload payload) {
		if(Flashback.RECORDER != null) {
			Flashback.RECORDER.writePacketAsync(
					ServerPlayNetworking.createS2CPacket(payload),
					NetworkPhase.PLAY
			);

		}
	}
}
