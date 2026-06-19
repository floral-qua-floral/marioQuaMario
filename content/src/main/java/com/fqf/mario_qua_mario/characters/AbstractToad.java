package com.fqf.mario_qua_mario.characters;

import com.fqf.charaformact_api.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.fqf.mario_qua_mario.util.Powers;
import com.google.common.collect.ImmutableSet;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.*;

public abstract class AbstractToad extends AbstractMarioSeriesCharacter implements CharacterDefinition {
	@Override
	public float defineHeightFactor() {
		return 0.97F;
	}

	@Override
	public float defineEyeHeightFactor() {
		return 0.765F;
	}

	@Override
	public float defineAnimationVerticalScale() {
		return 0.8F;
	}

	@Override
	public @NotNull SoundEvent defineJumpSound() {
		return MarioSFX.TOAD_JUMP;
	}

	@Override
	public void accumulatePowers(ImmutableSet.Builder<String> builder) {
		super.accumulatePowers(builder);
		builder.add(Powers.MYCOLOGICAL);
	}
}
