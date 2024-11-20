package com.floralquafloral.mariodata;

import com.floralquafloral.definitions.actions.CharaStat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

public interface MarioData {
	PlayerEntity getMario();
	boolean isClient();
	boolean useMarioPhysics();

	double getStat(CharaStat stat);
	double getStatMultiplier(CharaStat stat);
	int getBumpStrengthModifier();
	boolean isSneakProhibited();



	boolean isEnabled();
	Identifier getActionID();
	Identifier getPowerUpID();
	Identifier getCharacterID();

}
