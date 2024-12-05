package com.fqf.mario_qua_mario.mariodata;

public interface IMarioTravelData extends IMarioData {
	double getForwardVel();
	double getStrafeVel();
	double getYVel();

	void setForwardVel(double forward);
	void setStrafeVel(double strafe);
	default void setForwardStrafeVel(double forward, double strafe) {
		this.setForwardVel(forward);
		this.setStrafeVel(strafe);
	}
	void setYVel(double vertical);

	abstract class MarioInputs {
		public final MarioButton JUMP;
		public final MarioButton DUCK;
		public final MarioButton SPIN;

		public abstract double getForwardInput();
		public abstract double getStrafeInput();

		public abstract boolean isReal();

		public interface MarioButton {
			boolean isPressed();
			boolean isHeld();
		}

		protected MarioInputs(MarioButton jump, MarioButton duck, MarioButton spin) {
			JUMP = jump;
			DUCK = duck;
			SPIN = spin;
		}
	}
}
