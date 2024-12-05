package com.fqf.mario_qua_mario.mariodata;

import net.minecraft.util.Identifier;

public interface IMarioAuthoritativeData {
	boolean setAction(Identifier actionID);
	boolean setAction(String actionID);

	boolean setActionTransitionless(Identifier actionID);
	boolean setActionTransitionless(String actionID);

	boolean setPowerUp(Identifier powerUpID);
	boolean setPowerUp(String powerUpID);

	boolean setCharacter(Identifier characterID);
	boolean setCharacter(String characterID);
}
