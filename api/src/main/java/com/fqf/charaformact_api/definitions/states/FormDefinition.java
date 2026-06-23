package com.fqf.charaformact_api.definitions.states;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FormDefinition extends StatAlteringStateDefinition, AttackInterceptingStateDefinition {
	default @Nullable Identifier defineReversionTarget() {
		return null;
	}
	default int defineValue() {
		return 1;
	}

	default boolean doFlickerAnimation() {
		return true;
	}
	default @Nullable SoundEvent defineReversionSound() {
		return null;
	}
	default @Nullable SoundEvent defineAcquisitionSound() {
		return null;
	}
	default float defineVoicePitch() {
		return 1;
	}
	default float defineJumpPitch() {
		return 1;
	}

	default @NotNull FormDefinition.FormHeart defineFormHeart(FormHeartHelper helper) {
		return helper.vanilla();
	}

	interface FormHeartHelper {
		/**
		 * @return A FormHeart instance that uses the vanilla heart textures.
		 */
		FormHeart vanilla();

		/**
		 * @param namespace The desired namespace for the Form Heart textures.
		 * @param folder The desired sub-folder for the Form Heart textures.
		 * @return A Form Heart instance that uses the vanilla heart containers, and fills them in with textures at:
		 * <ul>
		 * <li>assets/(<code>namespace</code>)/textures/gui/sprites/hud/form_hearts/<code>folder</code>/full.png</li>
		 * <li>assets/(<code>namespace</code>)/textures/gui/sprites/hud/form_hearts/<code>folder</code>/full_blinking.png</li>
		 * <li>assets/(<code>namespace</code>)/textures/gui/sprites/hud/form_hearts/<code>folder</code>/half.png</li>
		 * <li>assets/(<code>namespace</code>)/textures/gui/sprites/hud/form_hearts/<code>folder</code>/half_blinking.png</li>
		 * <li>assets/(<code>namespace</code>)/textures/gui/sprites/hud/form_hearts/<code>folder</code>/hardcore/full.png</li>
		 * <li>assets/(<code>namespace</code>)/textures/gui/sprites/hud/form_hearts/<code>folder</code>/hardcore/full_blinking.png</li>
		 * <li>assets/(<code>namespace</code>)/textures/gui/sprites/hud/form_hearts/<code>folder</code>/hardcore/half.png</li>
		 * <li>assets/(<code>namespace</code>)/textures/gui/sprites/hud/form_hearts/<code>folder</code>/hardcore/half_blinking.png</li>
		 * </ul>
		 */
		FormHeart standard(String namespace, String folder);

		/**
		 * @return A FormHeart created by calling standard() using the namespace and path of this form's ID as the
		 * <code>namespace</code> and <code>folder</code> parameters.
		 */
		FormHeart auto();

		/**
		 * @param root An Identifier composed of a namespace and path.
		 * @return A Form Heart instance that uses the vanilla heart containers, and fills them in with textures at:
		 * <ul>
		 * <li>assets/(<code>namespace</code>)/textures/gui/sprites/<code>path</code>/full.png</li>
		 * <li>assets/(<code>namespace</code>)/textures/gui/sprites/<code>path</code>/full_blinking.png</li>
		 * <li>assets/(<code>namespace</code>)/textures/gui/sprites/<code>path</code>/half.png</li>
		 * <li>assets/(<code>namespace</code>)/textures/gui/sprites/<code>path</code>/half_blinking.png</li>
		 * <li>assets/(<code>namespace</code>)/textures/gui/sprites/<code>path</code>/hardcore/full.png</li>
		 * <li>assets/(<code>namespace</code>)/textures/gui/sprites/<code>path</code>/hardcore/full_blinking.png</li>
		 * <li>assets/(<code>namespace</code>)/textures/gui/sprites/<code>path</code>/hardcore/half.png</li>
		 * <li>assets/(<code>namespace</code>)/textures/gui/sprites/<code>path</code>/hardcore/half_blinking.png</li>
		 * </ul>
		 */
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
