package com.fqf.mario_qua_mario.definitions.states;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PowerUpDefinition extends StatAlteringStateDefinition, AttackInterceptingStateDefinition {
	@Nullable Identifier getReversionTarget();
	int getValue();

	@Nullable SoundEvent getAcquisitionSound();
	float getVoicePitch();

	@NotNull PowerHeart getPowerHeart(PowerHeartHelper helper);

	interface PowerHeartHelper {
		PowerHeart auto();

		PowerHeart standard(String namespace, String folder);

		PowerHeart fromRoot(Identifier root);
	}

	record PowerHeart(
			Identifier fullTexture, Identifier fullBlinkingTexture,
			Identifier halfTexture, Identifier halfBlinkingTexture,
			Identifier hardcoreFullTexture, Identifier hardcoreFullBlinkingTexture,
			Identifier hardcoreHalfTexture, Identifier hardcoreHalfBlinkingTexture,
			Identifier containerTexture, Identifier containerBlinkingTexture
	) {

	}
}
