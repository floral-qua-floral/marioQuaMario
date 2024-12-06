package com.fqf.mario_qua_mario.mariodata;

public interface IMarioTravelData extends IMarioReadableMotionData {
	void setForwardVel(double forward);
	void setStrafeVel(double strafe);
	default void setForwardStrafeVel(double forward, double strafe) {
		this.setForwardVel(forward);
		this.setStrafeVel(strafe);
	}
	void setYVel(double vertical);
}
