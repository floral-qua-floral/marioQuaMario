package com.fqf.charaformact_api.cfadata;

import com.fqf.charaformact_api.cfadata.util.RecordedCollisionSet;
import net.minecraft.util.math.Vec3d;

public interface CfaReadableMotionData extends CfaData {
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
		public abstract double getNormalizedForwardInput();
		public abstract double getNormalizedStrafeInput();

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
}
