package com.floralquafloral.mariodata;

import net.minecraft.client.network.ClientPlayerEntity;

public class MarioClientData extends MarioPlayerData {
	private static MarioClientData instance;
	public static MarioClientData getInstance() {
		return instance;
	}

	public MarioClientData(ClientPlayerEntity mario) {
		super(mario);
		MarioClientData.instance = this;
	}

	public int stateTimer;
	@Override public void setState(String state) {
		super.setState(state);
		stateTimer = 0;
	}


	public boolean travel() {


		return false;
	}
}
