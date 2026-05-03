package com.fqf.charapoweract.cpadata.util;

import com.fqf.charapoweract.cpadata.CPAPlayerData;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class FadeableSoundInstance extends PositionedSoundInstance implements TickableSoundInstance {
	private boolean isFading = false;

	public FadeableSoundInstance(CPAPlayerData mario) {
		this(mario.getCharacter().JUMP_SOUND, mario.getPlayer(), 1F, mario.getPowerForm().JUMP_PITCH);
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
