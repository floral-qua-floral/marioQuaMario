package com.fqf.charapoweract.cpadata.util;

import com.fqf.charapoweract.cpadata.ICPAClientDataImpl;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public abstract class AbstractSlidingSoundInstance extends MovingSoundInstance {
	protected final ICPAClientDataImpl DATA;
	protected final AbstractClientPlayerEntity PLAYER;
	private final SoundEvent SOUND_EVENT;
	protected int ticks;

	public AbstractSlidingSoundInstance(SoundEvent soundEvent, ICPAClientDataImpl data) {
		super(soundEvent, SoundCategory.PLAYERS, data.getPlayer().getRandom());
		this.DATA = data;
		this.PLAYER = data.getPlayer();
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
		if(!this.PLAYER.isRemoved() && this.equals(this.DATA.getStoredSounds().get(ICPAClientDataImpl.SLIDE_IDENTIFIER))) {
			if(this.isFloorDependent() && this.DATA.getFloorSlidingSound() != this.SOUND_EVENT) {
				this.setDone();
				this.DATA.handleSlidingSound(this.PLAYER.cpa$getCPAData().getAction());
			}
			else {
				this.ticks++;
				this.x = this.PLAYER.getX();
				this.y = this.PLAYER.getY();
				this.z = this.PLAYER.getZ();
				this.updatePitchVolume();
			}
		}
		else this.setDone();
	}

	protected abstract boolean isFloorDependent();
	protected abstract void updatePitchVolume();
}
