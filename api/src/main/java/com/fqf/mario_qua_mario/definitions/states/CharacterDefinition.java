package com.fqf.mario_qua_mario.definitions.states;

import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface CharacterDefinition extends StatAlteringStateDefinition {
	default @NotNull String getVoiceName() {
		return this.getID().getPath();
	}

	@NotNull Identifier getInitialAction();
	@NotNull Identifier getInitialPowerUp();

	@NotNull SoundEvent getJumpSound();
	@NotNull Identifier getMountedAction(Entity vehicle);
}
