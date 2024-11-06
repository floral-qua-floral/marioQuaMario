package com.floralquafloral.mariodata.client;

import com.floralquafloral.mariodata.MarioPlayerData;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

public abstract class AbstractMarioClientData extends MarioPlayerData {
	public AbstractMarioClientData(ClientPlayerEntity mario) {
		super(mario);
	}
}
