package com.fqf.mario_qua_mario.mariodata.util;

import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class FadeableSoundInstance extends PositionedSoundInstance implements TickableSoundInstance {
	private boolean isFading = false;

	public FadeableSoundInstance(MarioPlayerData mario) {
		this(mario.getCharacter().JUMP_SOUND, mario.getMario(), 1F, mario.getPowerUp().JUMP_PITCH);
	}
	public FadeableSoundInstance(SoundEvent sound, PlayerEntity mario, float volume, float pitch) {
		super(sound, SoundCategory.PLAYERS, volume, pitch, mario.getRandom(), mario.getX(), mario.getY(), mario.getZ());
	}

	public void fade() {
		this.isFading = true;
	}

	@Override
	public boolean isDone() {
		return this.volume <= 0;
	}

	@Override
	public void tick() {
		if(this.isFading) this.volume -= 0.2F;
	}
}
