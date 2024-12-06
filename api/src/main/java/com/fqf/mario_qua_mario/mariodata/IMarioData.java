package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.util.CharaStat;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.Set;

public interface IMarioData {
	PlayerEntity getMario();
	boolean isClient();

	boolean isEnabled();
	Identifier getActionID();
	Identifier getPowerUpID();
	Identifier getCharacterID();

	double getStat(CharaStat stat);
	double getStatMultiplier(CharaStat stat);
	int getBumpStrengthModifier();
}
