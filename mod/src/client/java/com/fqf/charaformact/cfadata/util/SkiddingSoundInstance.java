package com.fqf.charaformact.cfadata.util;

import com.fqf.charaformact.cfadata.CfaClientDataImpl;
import net.minecraft.sound.SoundEvent;

public class SkiddingSoundInstance extends AbstractSlidingSoundInstance {
	public SkiddingSoundInstance(SoundEvent soundEvent, CfaClientDataImpl data) {
		super(soundEvent, data);
	}

	@Override
	protected boolean isFloorDependent() {
		return true;
	}

	@Override
	protected void updatePitchVolume() {
		float slidingSpeed = (float) this.PLAYER.cfa$getCfaData().getHorizVelSquared();
		this.volume = Math.min(1.0F, ((float) this.ticks) / 3.0F) * Math.min(1.0F, 0.4F + 0.7F * slidingSpeed) * 0.4F;
		this.pitch = 1.0F + Math.min(0.15F, 0.5F * slidingSpeed);
	}
}
