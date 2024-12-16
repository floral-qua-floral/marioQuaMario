package com.fqf.mario_qua_mario.mariodata;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public interface IMarioAuthoritativeData extends IMarioData {
	@Override ServerPlayerEntity getMario();

	void setEnabled(boolean enable);

	boolean setAction(Identifier actionID);
	boolean setAction(String actionID);

	void setActionTransitionless(Identifier actionID);
	void setActionTransitionless(String actionID);

	void setPowerUp(Identifier powerUpID);
	void setPowerUp(String powerUpID);

	void setCharacter(Identifier characterID);
	void setCharacter(String characterID);
}
