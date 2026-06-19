package com.fqf.mario_qua_mario.characters;

import com.fqf.charaformact_api.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Toadette extends AbstractToad implements CharacterDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("toadette");
	@Override public @NotNull Identifier defineID() {
	    return ID;
	}

	@Override
	public @NotNull SoundEvent defineJumpSound() {
		return MarioSFX.TOADETTE_JUMP;
	}
}
