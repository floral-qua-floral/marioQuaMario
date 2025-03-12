package com.fqf.mario_qua_mario.characters;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Toadette extends AbstractMario implements CharacterDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("toadette");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public @NotNull SoundEvent getJumpSound() {
		return MarioContentSFX.TOADETTE_JUMP;
	}

	@Override
	public Set<StatModifier> getStatModifiers() {
		// Toadette has unmodified stats like Mario
		return Set.of();
	}
}
