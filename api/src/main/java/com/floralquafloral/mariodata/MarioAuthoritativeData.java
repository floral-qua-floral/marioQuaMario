package com.floralquafloral.mariodata;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Represents server-sided MarioData. The functions defined in this interface are networked to the player who this data
 * belongs to, and anyone who's tracking them.
 */
public interface MarioAuthoritativeData extends MarioTravelData {
	@Override
	ServerPlayerEntity getMario();

	/**
	 * @param isEnabled Whether the player should have access to Mario abilities.
	 */
	void setEnabled(boolean isEnabled);

	/**
	 * @param id The namespaced ID of the Character to switch to.
	 */
	void setCharacter(Identifier id);
	void setCharacter(String id);

	/**
	 * Changes Mario's currently-equipped power-up. This won't trigger transition-related sound effects or animations.
	 * @param id The namespaced ID of the Power-up to switch to.
	 */
	void setPowerUp(Identifier id);
	void setPowerUp(String id);

	/**
	 * Changes Mario's action. This will trigger whatever Action Transition the current action has which leads
	 * into the target action.
	 * <p>
	 * If no transition is found, the behavior of this method call depends on the rejectInvalidActionTransitions
	 * gamerule. If the gamerule is true, then the change is rejected and Mario remains in his current action. If
	 * it's false, then the change is allowed and Mario will be switched to the new action without any transitions
	 * occurring. See also: {@linkplain #setActionTransitionless}
	 * @param id The namespaced ID of the Action to transition to.
	 * @return True if Mario's action was changed, false otherwise.
	 */
	boolean setAction(Identifier id);
	boolean setAction(String id);

	/**
	 * Changes Mario's action. Action Transitions will not be checked for or executed.
	 * @param id The namespaced ID of the Action to switch to.
	 */
	void setActionTransitionless(Identifier id);
	void setActionTransitionless(String id);
}
