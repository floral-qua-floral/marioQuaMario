package com.fqf.mario_qua_mario.characters;

import com.fqf.charaformact_api.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.fqf.mario_qua_mario.util.Powers;
import com.google.common.collect.ImmutableSet;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class Mario extends AbstractMarioSeriesCharacter implements CharacterDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("mario");

	@Override public @NotNull String defineVoiceName() {
		return "mario";
	}

	@Override
	public float defineHeightFactor() {
		return 0.97F;
	}

	@Override
	public float defineEyeHeightFactor() {
		return 0.96F;
	}

	@Override
	public @NotNull SoundEvent defineJumpSound() {
		return MarioSFX.MARIO_JUMP;
	}

	@Override
	public void accumulatePowers(ImmutableSet.Builder<String> builder) {
		super.accumulatePowers(builder);
		builder.add(Powers.SLEEPY);
	}
}
