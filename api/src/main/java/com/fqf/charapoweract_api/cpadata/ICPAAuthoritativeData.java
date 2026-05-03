package com.fqf.charapoweract_api.cpadata;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ICPAAuthoritativeData extends ICPAData {
	@Override ServerPlayerEntity getPlayer();

	void disable();

	ActionTransitionResult forceActionTransition(@Nullable Identifier fromID, @NotNull Identifier toID);
	ActionTransitionResult forceActionTransition(@Nullable String fromID, @NotNull String toID);

	ActionChangeOperationResult assignAction(Identifier actionID);
	ActionChangeOperationResult assignAction(String actionID);

	PowerChangeOperationResult empowerTo(Identifier powerUpID);
	PowerChangeOperationResult empowerTo(String powerUpID);

	ReversionResult executeReversion();
	PowerChangeOperationResult revertTo(Identifier powerUpID);
	PowerChangeOperationResult revertTo(String powerUpID);

	PowerChangeOperationResult assignPowerForm(Identifier powerUpID);
	PowerChangeOperationResult assignPowerForm(String powerUpID);

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
