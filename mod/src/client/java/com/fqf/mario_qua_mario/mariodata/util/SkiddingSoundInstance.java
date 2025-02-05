package com.fqf.mario_qua_mario.mariodata.util;

import com.fqf.mario_qua_mario.mariodata.IMarioClientDataImpl;
import net.minecraft.sound.SoundEvent;

public class SkiddingSoundInstance extends AbstractSlidingSoundInstance {
	public SkiddingSoundInstance(SoundEvent soundEvent, IMarioClientDataImpl data) {
		super(soundEvent, data);
	}

	@Override
	protected boolean isFloorDependent() {
		return true;
	}

	@Override
	protected void updatePitchVolume() {
		float slidingSpeed = (float) this.MARIO.mqm$getMarioData().getHorizVelSquared();
		this.volume = Math.min(1.0F, ((float) this.ticks) / 3.0F) * Math.min(1.0F, 0.4F + 0.7F * slidingSpeed);
		this.pitch = 1.0F + Math.min(0.15F, 0.5F * slidingSpeed);
	}
}
