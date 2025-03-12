package com.fqf.mario_qua_mario.characters;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import com.fqf.mario_qua_mario.util.MarioVars;
import com.fqf.mario_qua_mario.util.Powers;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
				Powers.LIGHTNING_SHRINKS,
				Powers.SLEEPY
		);
	}
}
