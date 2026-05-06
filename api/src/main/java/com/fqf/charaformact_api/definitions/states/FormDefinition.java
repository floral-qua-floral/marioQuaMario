package com.fqf.charaformact_api.definitions.states;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FormDefinition extends StatAlteringStateDefinition, AttackInterceptingStateDefinition {
	@Nullable Identifier getReversionTarget();
	int getValue();

	@Nullable SoundEvent getAcquisitionSound();
	float getVoicePitch();
	float getJumpPitch();

	@NotNull FormDefinition.FormHeart getFormHeart(FormHeartHelper helper);

	interface FormHeartHelper {
		FormHeart auto();

		FormHeart standard(String namespace, String folder);

		FormHeart fromRoot(Identifier root);
	}

	record FormHeart(
			Identifier fullTexture, Identifier fullBlinkingTexture,
			Identifier halfTexture, Identifier halfBlinkingTexture,
			Identifier hardcoreFullTexture, Identifier hardcoreFullBlinkingTexture,
			Identifier hardcoreHalfTexture, Identifier hardcoreHalfBlinkingTexture,
			Identifier containerTexture, Identifier containerBlinkingTexture
	) {

	}
}
