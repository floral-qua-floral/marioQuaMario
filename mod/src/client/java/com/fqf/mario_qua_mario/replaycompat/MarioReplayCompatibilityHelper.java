package com.fqf.mario_qua_mario.replaycompat;

import com.replaymod.recording.ReplayModRecording;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;

public class MarioReplayCompatibilityHelper {
	public static void saveS2CPacketToReplay(CustomPayload payload) {
		if(ReplayModRecording.instance != null) {
			ReplayModRecording.instance.getConnectionEventHandler().getPacketListener().save(
					ServerPlayNetworking.createS2CPacket(payload)
			);
		}
	}
}
