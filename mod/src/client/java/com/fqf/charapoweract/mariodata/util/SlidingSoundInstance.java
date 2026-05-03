package com.fqf.charapoweract.mariodata.util;

import com.fqf.charapoweract.mariodata.IMarioClientDataImpl;
import net.minecraft.sound.SoundEvent;

public class SlidingSoundInstance extends AbstractSlidingSoundInstance {
	public SlidingSoundInstance(SoundEvent soundEvent, IMarioClientDataImpl data) {
		super(soundEvent, data);
	}

	@Override
	protected boolean isFloorDependent() {
		return true;
	}

	@Override
	protected void updatePitchVolume() {
		float slidingSpeed = (float) this.MARIO.mqm$getMarioData().getHorizVelSquared();
		this.volume = Math.min(1.0F, ((float) this.ticks) / 3.0F) * Math.min(1.0F, 0.7F * slidingSpeed);
		this.pitch = 1.0F + Math.min(0.15F, 0.5F * slidingSpeed);
	}
}
