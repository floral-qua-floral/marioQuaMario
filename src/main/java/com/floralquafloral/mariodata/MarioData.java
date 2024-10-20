package com.floralquafloral.mariodata;

import com.floralquafloral.registries.states.action.ParsedAction;
import com.floralquafloral.registries.states.character.ParsedCharacter;
import com.floralquafloral.registries.states.powerup.ParsedPowerUp;
import net.minecraft.entity.player.PlayerEntity;

public interface MarioData {
	PlayerEntity getMario();
	boolean useMarioPhysics();

	void setForwardVel(double forward);
	void setStrafeVel(double strafe);
	default void setForwardStrafeVel(double forward, double strafe) {
		this.setForwardVel(forward);
		this.setStrafeVel(strafe);
	}
	void setYVel(double vertical);
	double getForwardVel();
	double getStrafeVel();
	double getYVel();
	void applyModifiedVelocity();

	boolean isEnabled();
	void setEnabled(boolean enabled);
	ParsedAction getAction();
	boolean getSneakProhibited();
	void setAction(ParsedAction action, long seed);
	void setActionTransitionless(ParsedAction action);
	ParsedPowerUp getPowerUp();
	void setPowerUp(ParsedPowerUp powerUp);
	ParsedCharacter getCharacter();
	void setCharacter(ParsedCharacter character);
}
