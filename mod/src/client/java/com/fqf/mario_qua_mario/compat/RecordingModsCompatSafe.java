package com.fqf.mario_qua_mario.compat;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.packet.CustomPayload;

public class RecordingModsCompatSafe {
	public static final boolean REPLAY_MOD_PRESENT = FabricLoader.getInstance().isModLoaded("replaymod");
	public static final boolean FLASHBACK_PRESENT = FabricLoader.getInstance().isModLoaded("flashback");

	public static void conditionallySaveReplayPacket(CustomPayload payload) {
		if(REPLAY_MOD_PRESENT) ReplayModCompatUnsafe.saveS2CPacketToReplay(payload);
		if(FLASHBACK_PRESENT) FlashbackCompatUnsafe.saveS2CPacketToReplay(payload);
	}
}
