package com.floralquafloral.definitions;

import com.floralquafloral.mariodata.MarioData;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface PowerUpDefinition extends MarioMajorStateDefinition {
	@NotNull Map<String, String> getCharacterPlayermodels();

	void acquirePower(MarioData data);
	void losePower(MarioData data);

	int getValue();

	float getVoicePitch();

	@Nullable SoundEvent getAcquisitionSound();

	@Nullable Identifier getRevertTarget();

	@NotNull PowerHeart getHeart();
	@Nullable PowerHeart getHeartHardcore();
	@Nullable PowerHeartEmpty getHeartEmpty();

	class PowerHeart {
		private final Identifier FULL_TEXTURE;
		private final Identifier FULL_BLINKING_TEXTURE;
		private final Identifier HALF_TEXTURE;
		private final Identifier HALF_BLINKING_TEXTURE;

		public PowerHeart(
				Identifier fullTexture, Identifier fullBlinkingTexture,
				Identifier halfTexture, Identifier halfBlinkingTexture
		) {
			this.FULL_TEXTURE = fullTexture;
			this.FULL_BLINKING_TEXTURE = fullBlinkingTexture;
			this.HALF_TEXTURE = halfTexture;
			this.HALF_BLINKING_TEXTURE = halfBlinkingTexture;
		}

		public PowerHeart(String modID, String name) {
			this(
					Identifier.of(modID, "hud/power_hearts/" + name + "/full"),
					Identifier.of(modID, "hud/power_hearts/" + name + "/full_blinking"),
					Identifier.of(modID, "hud/power_hearts/" + name + "/half"),
					Identifier.of(modID, "hud/power_hearts/" + name + "/half_blinking")
			);
		}

		public Identifier getTexture(boolean half, boolean blinking) {
			if(blinking) return half ? HALF_BLINKING_TEXTURE : FULL_BLINKING_TEXTURE;
			else return half ? HALF_TEXTURE : FULL_TEXTURE;
		}
	}

	class PowerHeartEmpty extends PowerHeart {
		public PowerHeartEmpty(Identifier fullTexture, Identifier fullBlinkingTexture) {
			super(fullTexture, fullBlinkingTexture, fullTexture, fullBlinkingTexture);
		}
		public PowerHeartEmpty(String modID, String name) {
			this(Identifier.of(modID, "hud/power_hearts/" + name + "/container"),
					Identifier.of(modID, "hud/power_hearts/" + name + "/container_blinking"));
		}
	}
}
