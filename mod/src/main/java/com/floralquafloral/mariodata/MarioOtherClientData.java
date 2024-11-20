package com.floralquafloral.mariodata;

import net.minecraft.entity.player.PlayerEntity;

public class MarioOtherClientData extends MarioPlayerData implements MarioClientSideDataImplementation {
	public MarioOtherClientData(PlayerEntity mario) {
		super(mario);
	}

	@Override
	public void tick() {
		this.getAction().clientTick(this, false);
		this.getPowerUp().clientTick(this, false);
		this.getCharacter().clientTick(this, false);
	}
}
