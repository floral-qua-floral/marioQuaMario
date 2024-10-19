package com.floralquafloral.mariodata;

import com.floralquafloral.registries.action.ParsedAction;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	void setAction(ParsedAction action);
	void setActionTransitionless(ParsedAction action);
	String getPowerUp();
	void setPowerUp(String powerUp);
	String getCharacter();
	void setCharacter(String character);
}
