package com.fqf.mario_qua_mario.mariodata;

import net.minecraft.util.Pair;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMarioTravelData extends IMarioReadableMotionData {
	void setForwardVel(double forward);
	void setStrafeVel(double strafe);
	default void setForwardStrafeVel(double forward, double strafe) {
		this.setForwardVel(forward);
		this.setStrafeVel(strafe);
	}
	void setYVel(double vertical);

	void approachAngleAndAccel(
			double forwardAccel, double forwardTarget, double strafeAccel, double strafeTarget,
			double forwardAngleContribution, double strafeAngleContribution, double redirectDelta
	);

	@NotNull MarioTimers getTimers();
	class MarioTimers {
		public int actionTimer = 0;
		public int jumpLandingTime = 0;
		public int doubleJumpLandingTime = 0;

		public boolean jumpCapped = false;

		public boolean actionInterceptedAttack = false;

		public boolean bumpedCeiling = false;
		public boolean bumpedFloor = false;
		public @Nullable WallBumpReaction bumpedWall = null;

		public record WallBumpReaction(Direction direction, Vec3d originalVelocity) { }
	}
}
