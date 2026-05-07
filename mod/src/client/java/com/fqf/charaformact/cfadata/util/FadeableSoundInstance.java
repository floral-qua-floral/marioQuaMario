package com.fqf.charaformact.cfadata.util;

import com.fqf.charaformact.cfadata.CfaPlayerData;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class FadeableSoundInstance extends PositionedSoundInstance implements TickableSoundInstance {
	private boolean isFading = false;

	public FadeableSoundInstance(CfaPlayerData data) {
		this(data.getCharacter().JUMP_SOUND, data.getPlayer(), 1F, data.getForm().JUMP_PITCH);
	}
	public FadeableSoundInstance(SoundEvent sound, PlayerEntity player, float volume, float pitch) {
		super(sound, SoundCategory.PLAYERS, volume, pitch, player.getRandom(), player.getX(), player.getY(), player.getZ());
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
