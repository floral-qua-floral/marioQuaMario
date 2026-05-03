package com.fqf.charapoweract_api.cpadata;

import com.fqf.charapoweract_api.cpadata.util.RecordedCollisionSet;
import net.minecraft.util.math.Vec3d;

public interface ICPAReadableMotionData extends ICPAData {
	double getForwardVel();
	double getStrafeVel();
	double getYVel();
	Vec3d getVelocity();

	double getHorizVel();
	double getHorizVelSquared();

	double getDeltaYaw();

	Inputs getInputs();

	RecordedCollisionSet getRecordedCollisions();

	abstract class Inputs {
		public final ButtonInput JUMP;
		public final ButtonInput DUCK;
		public final ButtonInput SPIN;

		public abstract double getForwardInput();
		public abstract double getStrafeInput();

		public abstract boolean isReal();

		public interface ButtonInput {
			boolean isPressed();
			boolean isHeld();
		}

		protected Inputs(ButtonInput jump, ButtonInput duck, ButtonInput spin) {
			JUMP = jump;
			DUCK = duck;
			SPIN = spin;
		}
	}

	class MarioPublicVariables {
		public final long[] ACTION_TIMERS = new long[8];
		public final long[] POWER_UP_TIMERS = new long[8];

		public boolean jumpCapped;
	}
}
