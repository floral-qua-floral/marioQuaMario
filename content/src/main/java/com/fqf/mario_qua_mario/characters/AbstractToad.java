package com.fqf.mario_qua_mario.characters;

import com.fqf.charaformact_api.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.fqf.mario_qua_mario.util.Powers;
import com.google.common.collect.ImmutableSet;
import net.minecraft.sound.SoundEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.*;

public abstract class AbstractToad extends AbstractMario implements CharacterDefinition {
//	@Override public @NotNull String getVoiceName() {
//		return "toad";
//	}

	@Override
	public float getHeightFactor() {
		return 0.97F;
	}

	@Override
	public float getEyeHeightFactor() {
		return 0.765F;
	}

	@Override
	public float getAnimationVerticalScale() {
		return 0.8F;
	}

	@Override
	public @NotNull SoundEvent getJumpSound() {
		return MarioSFX.TOAD_JUMP;
	}

	@Override public Set<StatModifier> getStatModifiers() {
		return Set.of(
				// Blue Toad walks and runs EXTRA faster
				new StatModifier(Set.of(FORWARD, WALKING, SPEED), 1.3),
				new StatModifier(Set.of(FORWARD, RUNNING, SPEED), 1.34),
				new StatModifier(Set.of(FORWARD, P_RUNNING, SPEED), 1.34),

				// Blue Toad's jumps are a lot shorter
				new StatModifier(Set.of(JUMPING_GRAVITY), 1.1),
				new StatModifier(Set.of(JUMP_VELOCITY), 0.885)
		);
	}

	@Override
	public Set<String> getPowers() {
		ImmutableSet.Builder<String> marioPowers = ImmutableSet.builder();
		return marioPowers.addAll(super.getPowers()).add(Powers.MYCOLOGICAL).build();
	}
}
