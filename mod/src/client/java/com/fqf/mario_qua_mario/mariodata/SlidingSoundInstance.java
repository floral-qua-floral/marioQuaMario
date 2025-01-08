package com.fqf.mario_qua_mario.mariodata;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class SlidingSoundInstance extends MovingSoundInstance {
	private final IMarioClientDataImpl DATA;
	private final AbstractClientPlayerEntity MARIO;

	protected SlidingSoundInstance(SoundEvent soundEvent, IMarioClientDataImpl data) {
		super(soundEvent, SoundCategory.PLAYERS, data.getMario().getRandom());
		this.DATA = data;
		this.MARIO = (AbstractClientPlayerEntity) data.getMario();
	}

	@Override
	public boolean shouldAlwaysPlay() {
		return true;
	}

	@Override
	public void tick() {
		if(!this.MARIO.isRemoved() && this.DATA.getStoredSounds().get(IMarioClientDataImpl.SLIDE_IDENTIFIER).equals(this)) {
			this.x = this.MARIO.getX();
			this.y = this.MARIO.getY();
			this.z = this.MARIO.getZ();
		}
		else this.setDone();
	}
}
