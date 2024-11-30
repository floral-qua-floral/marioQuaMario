package com.floralquafloral.mariodata;

import net.minecraft.util.Pair;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MarioTravelData extends MarioData {
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

	@NotNull MarioInputs getInputs();

	void approachAngleAndAccel(
			double forwardAccel, double forwardTarget, double strafeAccel, double strafeTarget,
			double forwardAngleContribution, double strafeAngleContribution, double redirectDelta
	);

	abstract class MarioInputs {
		public final ButtonInput JUMP;
		public final ButtonInput DUCK;
		public final ButtonInput SPIN;

		public MarioInputs(
				@NotNull ButtonInput jumpInput,
				@NotNull ButtonInput duckInput,
				@NotNull ButtonInput spinInput
		) {
			this.JUMP = jumpInput;
			this.DUCK = duckInput;
			this.SPIN = spinInput;
		}

		public abstract boolean isReal();

		public abstract double getForwardInput();
		public abstract double getStrafeInput();

		public interface ButtonInput {
			boolean isHeld();
			boolean isPressed();
			boolean isPressedNoUnbuffer();
		}
	}

	@NotNull MarioTimers getTimers();
	class MarioTimers {
		public int actionTimer = 0;
		public int jumpLandingTime = 0;
		public int doubleJumpLandingTime = 0;

		public boolean jumpCapped = false;

		public boolean actionInterceptedAttack = false;

		public boolean bumpedCeiling = false;
		public boolean bumpedFloor = false;
		public @Nullable Pair<Direction, Vec3d> bumpedWall = null;
	}
}
