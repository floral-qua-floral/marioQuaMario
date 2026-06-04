package com.fqf.charaformact.cfadata.util;

import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class SustainingSoundInstance extends EntityAttachedSoundInstance {
	private boolean isSustaining;

	public SustainingSoundInstance(SoundEvent soundEvent, Entity entity, SoundCategory category, float pitch, float volume) {
		super(soundEvent, entity, category, pitch, volume);

		this.isSustaining = true;
		this.repeat = true;
	}

	@Override
	public void tick() {
		super.tick();
		if(this.isSustaining) this.isSustaining = false;
		else this.setDone();
	}

	public void sustain(float pitch, float volume) {
		this.isSustaining = true;
		this.pitch = pitch;
		this.volume = volume;
	}
}
