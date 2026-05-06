package com.fqf.charaformact_api.cfadata;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface CfaAuthoritativeData extends CfaData {
	@Override ServerPlayerEntity getPlayer();

	void disable();

	ActionTransitionResult forceActionTransition(@Nullable Identifier fromID, @NotNull Identifier toID);
	ActionTransitionResult forceActionTransition(@Nullable String fromID, @NotNull String toID);

	ActionChangeOperationResult assignAction(Identifier actionID);
	ActionChangeOperationResult assignAction(String actionID);

	FormChangeOperationResult empowerTo(Identifier formID);
	FormChangeOperationResult empowerTo(String formID);

	ReversionResult executeReversion();
	FormChangeOperationResult revertTo(Identifier formID);
	FormChangeOperationResult revertTo(String formID);

	FormChangeOperationResult assignForm(Identifier formID);
	FormChangeOperationResult assignForm(String formID);

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
	enum FormChangeOperationResult {
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
