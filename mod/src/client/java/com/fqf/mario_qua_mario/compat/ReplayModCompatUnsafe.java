package com.fqf.mario_qua_mario.compat;

import com.replaymod.recording.ReplayModRecording;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;

class ReplayModCompatUnsafe {
	static void saveS2CPacketToReplay(CustomPayload payload) {
		if(ReplayModRecording.instance != null && ReplayModRecording.instance.getConnectionEventHandler().getPacketListener() != null) {
			ReplayModRecording.instance.getConnectionEventHandler().getPacketListener().save(
					ServerPlayNetworking.createS2CPacket(payload)
			);
		}
	}
}
