package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.PlayermodelAnimation;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public interface IMarioClientData extends IMarioData {
	void playAnimation(PlayermodelAnimation animation, int ticks);

	SoundInstanceWrapper playSound(
			SoundEvent event, SoundCategory category,
			double x, double y, double z,
			float pitch, float volume, long seed
	);

	SoundInstanceWrapper playSound(SoundEvent event, long seed);
	SoundInstanceWrapper playSound(SoundEvent event, float pitch, float volume, long seed);
	SoundInstanceWrapper playSound(SoundEvent event, Entity entity, SoundCategory category, long seed);

	void playJumpSound(long seed);
	void fadeJumpSound();

	SoundInstanceWrapper voice(String voiceline, long seed);
	float getVoicePitch();

	void storeSound(SoundInstanceWrapper instance);
	void stopStoredSound(SoundEvent event);

	enum VoiceLine {
		SELECT,
		DUCK,

		DOUBLE_JUMP,
		TRIPLE_JUMP,
		GYMNAST_SALUTE,

		DUCK_JUMP,
		LONG_JUMP,
		BACKFLIP,
		SIDEFLIP,
		WALL_JUMP,

		REVERT,
		BURNT,

		FIREBALL,
		GET_STAR
	}

	interface SoundInstanceWrapper {
	}
}
