package com.fqf.mario_qua_mario.mariodata;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public interface IMarioAuthoritativeData extends IMarioData {
	@Override ServerPlayerEntity getMario();

	void disable();

	ActionTransitionResult transitionToAction(Identifier actionID);
	ActionTransitionResult transitionToAction(String actionID);

	ActionChangeOperationResult assignAction(Identifier actionID);
	ActionChangeOperationResult assignAction(String actionID);

	PowerChangeOperationResult empowerTo(Identifier powerUpID);
	PowerChangeOperationResult empowerTo(String powerUpID);

	ReversionResult executeReversion();
	PowerChangeOperationResult revertTo(Identifier powerUpID);
	PowerChangeOperationResult revertTo(String powerUpID);

	PowerChangeOperationResult assignPowerUp(Identifier powerUpID);
	PowerChangeOperationResult assignPowerUp(String powerUpID);

	void assignCharacter(@Nullable Identifier characterID);
	void assignCharacter(String characterID);

	enum ActionChangeOperationResult {
		SUCCESS,
		NOT_ENABLED
	}
	enum ActionTransitionResult {
		SUCCESS,
		NOT_ENABLED,
		NO_SUCH_TRANSITION
	}
	enum PowerChangeOperationResult {
		SUCCESS,
		NOT_ENABLED,
		MISSING_PLAYERMODEL
	}
	enum ReversionResult {
		SUCCESS,
		NOT_ENABLED,
		MISSING_PLAYERMODEL,
		NO_WEAKER_FORM
	}
}
