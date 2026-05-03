package com.fqf.mario_qua_mario_content.characters;

import com.fqf.charapoweract_api.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.util.MarioContentSFX;
import com.fqf.mario_qua_mario_content.util.Powers;
import com.google.common.collect.ImmutableSet;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Mario extends AbstractMario implements CharacterDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("mario");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public float getHeightFactor() {
		return 0.97F;
	}

	@Override
	public float getEyeHeightFactor() {
		return 0.96F;
	}

	@Override
	public @NotNull SoundEvent getJumpSound() {
		return MarioContentSFX.MARIO_JUMP;
	}

	@Override
	public Set<StatModifier> getStatModifiers() {
		return Set.of();
	}

	@Override
	public Set<String> getPowers() {
		ImmutableSet.Builder<String> marioPowers = ImmutableSet.builder();
		return marioPowers.addAll(super.getPowers()).add(Powers.SLEEPY).build();
	}
}
