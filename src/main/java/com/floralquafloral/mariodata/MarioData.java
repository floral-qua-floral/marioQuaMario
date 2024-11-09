package com.floralquafloral.mariodata;

import com.floralquafloral.registries.states.action.ParsedAction;
import com.floralquafloral.registries.states.character.ParsedCharacter;
import com.floralquafloral.registries.states.powerup.ParsedPowerUp;
import net.minecraft.entity.player.PlayerEntity;

public interface MarioData {
	PlayerEntity getMario();
	boolean isClient();
	boolean useMarioPhysics();

	boolean isEnabled();
	ParsedAction getAction();
	boolean getSneakProhibited();
	ParsedPowerUp getPowerUp();
	ParsedCharacter getCharacter();
}
