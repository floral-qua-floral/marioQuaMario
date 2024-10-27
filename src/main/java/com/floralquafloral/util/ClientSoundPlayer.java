package com.floralquafloral.util;

import com.floralquafloral.mariodata.MarioData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.random.Random;

@SuppressWarnings("UnusedReturnValue")
public abstract class ClientSoundPlayer {
	public static final SoundManager SOUND_MANAGER = MinecraftClient.getInstance().getSoundManager();

	public static PositionedSoundInstance playSound(SoundEvent event, SoundCategory category, double x, double y, double z, float volume, float pitch, long seed) {
		PositionedSoundInstance sound = new PositionedSoundInstance(
				event,
				category,
				volume,
				pitch,
				Random.create(seed),
				x,
				y,
				z
		);
		SOUND_MANAGER.play(sound);
		return sound;
	}

	public static PositionedSoundInstance playSound(SoundEvent event, SoundCategory category, Entity entity, float volume, float pitch, long seed) {
		return playSound(event, category, entity.getX(), entity.getY(), entity.getZ(), volume, pitch, seed);
	}

	public static PositionedSoundInstance playSound(SoundEvent event, MarioData data, float volume, float pitch, long seed) {
		return playSound(event, SoundCategory.PLAYERS, data.getMario(), volume, pitch, seed);
	}

	public static PositionedSoundInstance playSound(SoundEvent event, MarioData data, long seed) {
		return playSound(event, data, 1.0F, 1.0F, seed);
	}

	public static void kill(PositionedSoundInstance sound) {
		SOUND_MANAGER.stop(sound);
	}
}
