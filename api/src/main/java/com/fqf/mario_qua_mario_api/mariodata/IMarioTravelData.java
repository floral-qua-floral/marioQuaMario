package com.fqf.mario_qua_mario_api.mariodata;

import net.minecraft.util.math.Vec3d;

public interface IMarioTravelData extends IMarioReadableMotionData {
	void setForwardVel(double forward);
	void setStrafeVel(double strafe);
	default void setForwardStrafeVel(double forward, double strafe) {
		this.setForwardVel(forward);
		this.setStrafeVel(strafe);
	}
	void setYVel(double vertical);

	void setVelocity(Vec3d velocity);

	void approachAngleAndAccel(
			double forwardAccel, double forwardTarget, double strafeAccel, double strafeTarget,
			double forwardAngleContribution, double strafeAngleContribution, double redirectDelta
	);

	void refreshJumpCapping();
}
