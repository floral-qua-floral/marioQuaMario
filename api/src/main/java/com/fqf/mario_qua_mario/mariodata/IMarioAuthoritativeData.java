package com.fqf.mario_qua_mario.mariodata;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public interface IMarioAuthoritativeData extends IMarioData {
	@Override ServerPlayerEntity getMario();

	boolean setEnabled(boolean enable);

	boolean setAction(Identifier actionID);
	boolean setAction(String actionID);

	boolean setActionTransitionless(Identifier actionID);
	boolean setActionTransitionless(String actionID);

	boolean setPowerUp(Identifier powerUpID);
	boolean setPowerUp(String powerUpID);

	boolean setCharacter(Identifier characterID);
	boolean setCharacter(String characterID);
}
