package com.floralquafloral.mariodata;

import com.floralquafloral.registries.action.ParsedAction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MarioData {
	boolean useMarioPhysics();

	interface MarioVelocityContainer {
		double getForward();
		double getStrafe();
		double getVertical();
	}

	void tick();

	void setVelocities(double forward, double strafe, @Nullable Double vertical);
	void setVelocities(MarioVelocityContainer velocities);
	@NotNull MarioVelocityContainer getVelocities();
	void clearCachedVelocities();

	boolean isEnabled();
	void setEnabled(boolean enabled);
	ParsedAction getAction();
	void setAction(ParsedAction action);
	void setActionTransitionless(ParsedAction action);
	String getPowerUp();
	void setPowerUp(String powerUp);
	String getCharacter();
	void setCharacter(String character);
}
