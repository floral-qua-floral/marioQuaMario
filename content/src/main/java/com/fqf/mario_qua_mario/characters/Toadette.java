package com.fqf.mario_qua_mario.characters;

import com.fqf.charaformact_api.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Toadette extends AbstractMario implements CharacterDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("toadette");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

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
		return MarioSFX.TOADETTE_JUMP;
	}

	@Override
	public Set<StatModifier> getStatModifiers() {
		// Toadette has unmodified stats like Mario
		return Set.of();
	}
}
