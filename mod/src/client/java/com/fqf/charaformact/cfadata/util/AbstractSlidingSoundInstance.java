package com.fqf.charaformact.cfadata.util;

import com.fqf.charaformact.cfadata.CfaClientDataImpl;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public abstract class AbstractSlidingSoundInstance extends EntityAttachedSoundInstance {
	protected final AbstractClientPlayerEntity PLAYER;
	protected final CfaClientDataImpl DATA;
	private final SoundEvent SOUND_EVENT;
	protected int ticks;

	public AbstractSlidingSoundInstance(SoundEvent soundEvent, CfaClientDataImpl data) {
		super(soundEvent, data.getPlayer(), SoundCategory.PLAYERS, 1, 0);
		this.PLAYER = data.getPlayer();
		this.DATA = data;
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
		if(!this.PLAYER.isRemoved() && this.equals(this.DATA.getStoredSounds().get(CfaClientDataImpl.SLIDE_IDENTIFIER))) {
			if(this.isFloorDependent() && this.DATA.getFloorSlidingSound() != this.SOUND_EVENT) {
				this.setDone();
				this.DATA.handleSlidingSound(this.PLAYER.cfa$getCfaData().getAction());
			}
			else {
				this.ticks++;
				super.tick();
				this.updatePitchVolume();
			}
		}
		else this.setDone();
	}

	protected abstract boolean isFloorDependent();
	protected abstract void updatePitchVolume();
}
