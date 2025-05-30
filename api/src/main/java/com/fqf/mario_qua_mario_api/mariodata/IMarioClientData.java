package com.fqf.mario_qua_mario_api.mariodata;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public interface IMarioClientData extends IMarioData {
	void playAnimation(PlayermodelAnimation animation, int ticks);
	void playCameraAnimation(CameraAnimationSet animationSet);

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

	void instantVisualRotate(float rotationDegrees, boolean counterRotateAnimation);

	interface SoundInstanceWrapper {
	}
}
