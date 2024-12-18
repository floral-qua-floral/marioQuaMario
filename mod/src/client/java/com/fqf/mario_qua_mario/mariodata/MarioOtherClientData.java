package com.fqf.mario_qua_mario.mariodata;

import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

public class MarioOtherClientData extends MarioPlayerData implements IMarioClientDataImpl {
	private OtherClientPlayerEntity mario;
	public MarioOtherClientData(OtherClientPlayerEntity mario) {
		super();
		this.mario = mario;
	}
	@Override public OtherClientPlayerEntity getMario() {
		return this.mario;
	}
	@Override public void setMario(PlayerEntity mario) {
		this.mario = (OtherClientPlayerEntity) mario;
	}

	@Override
	public void tick() {
		super.tick();
		this.getAction().clientTick(this, false);
		this.getPowerUp().clientTick(this, false);
		this.getCharacter().clientTick(this, false);
	}
}
