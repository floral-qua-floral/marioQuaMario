package com.fqf.mario_qua_mario.mariodata;

public interface IMarioReadableMotionData extends IMarioData {
	double getForwardVel();
	double getStrafeVel();
	double getYVel();

	MarioInputs getInputs();

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
