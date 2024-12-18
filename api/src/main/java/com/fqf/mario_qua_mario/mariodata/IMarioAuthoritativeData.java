package com.fqf.mario_qua_mario.mariodata;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public interface IMarioAuthoritativeData extends IMarioData {
	@Override ServerPlayerEntity getMario();

	void setEnabled(boolean enable);

	boolean transitionToAction(Identifier actionID);
	boolean transitionToAction(String actionID);

	void assignAction(Identifier actionID);
	void assignAction(String actionID);

	void empowerTo(Identifier powerUpID);
	void empowerTo(String powerUpID);

	void revertTo(Identifier powerUpID);
	void revertTo(String powerUpID);

	void assignPowerUp(Identifier powerUpID);
	void assignPowerUp(String powerUpID);

	void assignCharacter(Identifier characterID);
	void assignCharacter(String characterID);
}
