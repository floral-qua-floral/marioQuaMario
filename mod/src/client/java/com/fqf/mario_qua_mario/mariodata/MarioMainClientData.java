package com.fqf.mario_qua_mario.mariodata;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

public class MarioMainClientData extends MarioPlayerData implements IMarioClientDataImpl {
	private static MarioMainClientData instance;
	public static @Nullable MarioMainClientData getInstance() {
		return instance;
	}
	public static void clearInstance() {
		instance = null;
	}

	private ClientPlayerEntity mario;
	public MarioMainClientData(ClientPlayerEntity mario) {
		this.mario = mario;
		instance = this;
	}

	@Override
	public ClientPlayerEntity getMario() {
		return mario;
	}

	@Override
	public void setMario(PlayerEntity mario) {
		this.mario = (ClientPlayerEntity) mario;
	}
}
