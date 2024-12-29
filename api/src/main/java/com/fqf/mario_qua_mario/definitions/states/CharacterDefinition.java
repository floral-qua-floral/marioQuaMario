package com.fqf.mario_qua_mario.definitions.states;

import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface CharacterDefinition extends StatAlteringStateDefinition {
	Identifier getInitialAction();
	Identifier getInitialPowerUp();

	SoundEvent getJumpSound();
	Identifier getMountedAction(Entity vehicle);
}
