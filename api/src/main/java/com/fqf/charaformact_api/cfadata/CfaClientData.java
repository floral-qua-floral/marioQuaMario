package com.fqf.charaformact_api.cfadata;

import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public interface CfaClientData extends CfaData {
	void playAnimation(AnimationDefinition animation, int duration);
	void playCameraAnimation(CameraAnimationSet animationSet);

	SoundInstanceWrapper playSound(
			SoundEvent event, SoundCategory category,
			double x, double y, double z,
			float pitch, float volume, long seed
	);

	SoundInstanceWrapper playSound(SoundEvent event, long seed);
	SoundInstanceWrapper playSound(SoundEvent event, float pitch, float volume, long seed);
	SoundInstanceWrapper playSound(SoundEvent event, Entity entity, SoundCategory category, long seed);

	void sustainSound(SoundEvent event, Entity entity, SoundCategory category);
	void sustainSound(SoundEvent event, Entity entity, SoundCategory category, float pitch, float volume);

	void playJumpSound(long seed);
	void fadeJumpSound();

	SoundInstanceWrapper voice(Identifier voiceline, long seed);
	SoundInstanceWrapper voice(Identifier voiceline, float volume, long seed);
	float getVoicePitch();

	void storeSound(SoundInstanceWrapper instance);
	void stopStoredSound(SoundEvent event);

	void instantVisualRotate(float rotationDegrees, boolean counterRotateAnimation);

	interface SoundInstanceWrapper {
	}
}
