package com.fqf.mario_qua_mario_content.characters;

import com.fqf.mario_qua_mario_api.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.util.MarioContentSFX;
import com.fqf.mario_qua_mario_content.util.Powers;
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
	public @NotNull SoundEvent getJumpSound() {
		return MarioContentSFX.MARIO_JUMP;
	}

	@Override
	public Set<StatModifier> getStatModifiers() {
		return Set.of();
	}

	@Override
	public Set<String> getPowers() {
		return Set.of(
				Powers.DROP_COINS,
				Powers.LIGHTNING_SHRINKS,
				Powers.CEILING_CLIPPING,
				Powers.SLEEPY
		);
	}
}
