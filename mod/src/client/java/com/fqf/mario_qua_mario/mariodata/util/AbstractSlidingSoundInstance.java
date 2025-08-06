package com.fqf.mario_qua_mario.mariodata.util;

import com.fqf.mario_qua_mario.mariodata.IMarioClientDataImpl;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public abstract class AbstractSlidingSoundInstance extends MovingSoundInstance {
	protected final IMarioClientDataImpl DATA;
	protected final AbstractClientPlayerEntity MARIO;
	private final SoundEvent SOUND_EVENT;
	protected int ticks;

	public AbstractSlidingSoundInstance(SoundEvent soundEvent, IMarioClientDataImpl data) {
		super(soundEvent, SoundCategory.PLAYERS, data.getMario().getRandom());
		this.DATA = data;
		this.MARIO = data.getMario();
		this.SOUND_EVENT = soundEvent;
		this.repeat = true;
		this.ticks = 0;
	}

	@Override
	public boolean shouldAlwaysPlay() {
		return true;
	}

	@Override
	public void tick() {
		if(!this.MARIO.isRemoved() && this.equals(this.DATA.getStoredSounds().get(IMarioClientDataImpl.SLIDE_IDENTIFIER))) {
			if(this.isFloorDependent() && this.DATA.getFloorSlidingSound() != this.SOUND_EVENT) {
				this.setDone();
				this.DATA.handleSlidingSound(this.MARIO.mqm$getMarioData().getAction());
			}
			else {
				this.ticks++;
				this.x = this.MARIO.getX();
				this.y = this.MARIO.getY();
				this.z = this.MARIO.getZ();
				this.updatePitchVolume();
			}
		}
		else this.setDone();
	}

	protected abstract boolean isFloorDependent();
	protected abstract void updatePitchVolume();
}
