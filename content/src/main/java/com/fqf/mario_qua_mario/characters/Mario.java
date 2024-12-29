package com.fqf.mario_qua_mario.characters;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.CharacterDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class Mario implements CharacterDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("mario");
	}

	@Override public Identifier getInitialAction() {
		return MarioQuaMarioContent.makeID("debug");
	}
	@Override public Identifier getInitialPowerUp() {
		return MarioQuaMarioContent.makeID("super");
	}

	@Override public Identifier getMountedAction(Entity vehicle) {
		return MarioQuaMarioContent.makeID("mounted");
	}
	@Override public SoundEvent getJumpSound() {
		return null;
	}

	@Override public float getWidthFactor() {
		return 1;
	}
	@Override public float getHeightFactor() {
		return 1;
	}

	@Override public int getBumpStrengthModifier() {
		return 0;
	}

	@Override public Set<String> getPowers() {
		return Set.of();
	}

	@Override public Set<StatModifier> getStatModifiers() {
		return Set.of();
	}

	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}

	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
}
