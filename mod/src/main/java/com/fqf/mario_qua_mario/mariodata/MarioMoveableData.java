package com.fqf.mario_qua_mario.mariodata;

import com.fqf.mario_qua_mario.definitions.states.actions.WallboundActionDefinition;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2d;

public abstract class MarioMoveableData extends MarioPlayerData implements IMarioTravelData {
	public boolean jumpCapped;

	public MarioMoveableData() {
		super();
	}

	private final MarioVelocities VELOCITIES = new MarioVelocities();
	private class MarioVelocities {
		private double forward;
		private double strafe;
		private double vertical;
		private double negativeSineYaw;
		private double cosineYaw;
		private boolean isGenerated;
		private boolean isDirty;

		private MarioVelocities ensure() {
			if(this.isGenerated) return this;
			this.isGenerated = true;

			// Calculate forward and sideways vector components
			double yawRad = Math.toRadians(getMario().getYaw());
			this.negativeSineYaw = -Math.sin(yawRad);
			this.cosineYaw = Math.cos(yawRad);

			// Calculate current forwards and sideways velocity
			Vec3d currentVel = getMario().getVelocity();
			this.forward = currentVel.x * negativeSineYaw + currentVel.z * cosineYaw;
			this.strafe = currentVel.x * cosineYaw + currentVel.z * -negativeSineYaw;
			this.vertical = currentVel.y;

			return this;
		}
		private MarioVelocities ensureDirty() {
			this.isDirty = true;
			return this.ensure();
		}
		private void apply() {
			if(!this.isGenerated || !this.isDirty) return;
			getMario().setVelocity(this.forward * this.negativeSineYaw + this.strafe * this.cosineYaw,
					this.vertical, this.forward * this.cosineYaw + this.strafe * -this.negativeSineYaw);
		}
	}

	@Override public double getForwardVel() {
		return this.VELOCITIES.ensure().forward;
	}
	@Override public double getStrafeVel() {
		return this.VELOCITIES.ensure().strafe;
	}
	@Override public double getYVel() {
		if(this.VELOCITIES.isGenerated) return this.VELOCITIES.vertical;
		else return this.getMario().getVelocity().y;
	}
	@Override public double getHorizVel() {
		return this.getMario().getVelocity().horizontalLength();
	}
	@Override public double getHorizVelSquared() {
		return this.getMario().getVelocity().horizontalLengthSquared();
	}

	@Override public double getDeltaYaw() {
		return this.smoothedDeltaYaw;
	}

	private double prevYaw;
	private double smoothedDeltaYaw;
	@Override public void tick() {
		super.tick();

		double deltaYaw = this.getMario().getYaw() - this.prevYaw;
		this.prevYaw = this.getMario().getYaw();
		this.smoothedDeltaYaw = MathHelper.lerp(0.2, this.smoothedDeltaYaw, deltaYaw);
//		double deltaYawDiff = deltaYaw - smoothedDeltaYaw;
//		if(Math.abs(deltaYawDiff) > 0.1) this.smoothedDeltaYaw += 0.1 * Math.signum(deltaYawDiff);
//		else this.smoothedDeltaYaw = deltaYaw;
	}

	@Override public void setForwardVel(double forward) {
		VELOCITIES.ensureDirty().forward = forward;
	}
	@Override public void setStrafeVel(double strafe) {
		this.VELOCITIES.ensureDirty().strafe = strafe;
	}
	@Override public void setYVel(double vertical) {
		if(vertical > 0) this.getMario().fallDistance = 0;
		if(this.VELOCITIES.isGenerated) this.VELOCITIES.ensureDirty().vertical = vertical;
		else {
			Vec3d oldVel = this.getMario().getVelocity();
			this.getMario().setVelocity(oldVel.x, vertical, oldVel.z);
		}
	}

	public void applyModifiedVelocity() {
		this.VELOCITIES.apply();
		this.VELOCITIES.isDirty = false;
		this.VELOCITIES.isGenerated = false;
	}

	@Override public void approachAngleAndAccel(
			double forwardAccel, double forwardTarget, double strafeAccel, double strafeTarget,
			double forwardAngleContribution, double strafeAngleContribution, double redirectDelta
	) {
		Vector2d redirectedVel;

		double forwardVel = getForwardVel();
		double strafeVel = getStrafeVel();

		if (redirectDelta == 0 || (forwardAngleContribution == 0 && strafeAngleContribution == 0) ||
				(MathHelper.approximatelyEquals(forwardVel, 0) && MathHelper.approximatelyEquals(strafeVel, 0))) {
			redirectedVel = new Vector2d(forwardVel, strafeVel);
		} else {
			Vector2d currentVel = new Vector2d(forwardVel, strafeVel);
			Vector2d intendedAngle = new Vector2d(forwardAngleContribution, strafeAngleContribution);

			if (redirectDelta > 0) redirectedVel = MarioMoveableData.slerp(currentVel, intendedAngle, redirectDelta);
			else
				redirectedVel = intendedAngle.normalize(currentVel.length()); // redirectAngle < 0 for instant redirection
		}

		Vector2d newVel;
		if (forwardAccel == 0 && strafeAccel == 0) {
			// If we're only redirecting then we're done here, no need to calculate acceleration & apply speed cap
			newVel = redirectedVel;
		} else {
			// Ensure forwardAccel and strafeAccel are positive
			forwardAccel = Math.abs(forwardAccel);
			strafeAccel = Math.abs(strafeAccel);

			// Calculate which way to accelerate
			double forwardAccelDir, strafeAccelDir;
			double forwardDifference = forwardTarget - redirectedVel.x;
			if (MathHelper.approximatelyEquals(forwardDifference, 0))
				forwardAccelDir = 0;
			else if (forwardAccel < Math.abs(forwardDifference))
				forwardAccelDir = Math.signum(forwardDifference);
			else {
				forwardAccelDir = 0;
				redirectedVel.x = forwardTarget;
			}
			double strafeDifference = strafeTarget - redirectedVel.y;
			if (MathHelper.approximatelyEquals(strafeDifference, 0))
				strafeAccelDir = 0;
			else if (strafeAccel < Math.abs(strafeDifference))
				strafeAccelDir = Math.signum(strafeDifference);
			else {
				strafeAccelDir = 0;
				redirectedVel.y = strafeTarget;
			}

			// Calculate the acceleration vector and normalize it, so the player won't get extra acceleration by strafing
			Vector2d accelVector = new Vector2d(
					forwardAccel * forwardAccelDir,
					strafeAccel * strafeAccelDir
			);
			if (accelVector.x != 0 || accelVector.y != 0) {
				double accelVectorMaxLength = Math.max(forwardAccel, strafeAccel);
				if (accelVector.lengthSquared() > accelVectorMaxLength * accelVectorMaxLength)
					accelVector.normalize(accelVectorMaxLength);
			}

			// Calculate the new velocity
			newVel = new Vector2d(
					redirectedVel.x + accelVector.x,
					redirectedVel.y + accelVector.y
			);

			// Calculate & apply soft speed cap
			double speedCap = Math.max(Math.abs(forwardTarget), Math.abs(strafeTarget));
			double speedCapSquared = speedCap * speedCap;
			double oldVelLengthSquared = Vector2d.lengthSquared(forwardVel, strafeVel);

			if (newVel.lengthSquared() > oldVelLengthSquared) {
				if (oldVelLengthSquared > speedCapSquared)
					newVel.normalize(Vector2d.length(forwardVel, strafeVel));
				else if (newVel.lengthSquared() > speedCapSquared)
					newVel.normalize(speedCap);
			}
		}

		// Apply the new velocities
		setForwardStrafeVel(newVel.x, newVel.y);
	}

	private static Vector2d slerp(Vector2d currentVelocity, Vector2d intendedAngle, double turnSpeedDegrees) {
		// Convert turnSpeed to radians
		double turnSpeedRadians = Math.toRadians(turnSpeedDegrees);

		// Normalize the input vectors (slerp typically operates on normalized vectors)
		Vector2d currentDir = new Vector2d(currentVelocity).normalize();
		Vector2d intendedDir = new Vector2d(intendedAngle).normalize();

		// Calculate the angle between the two vectors using the dot product
		double dotProduct = currentDir.dot(intendedDir);
		// Clamp the dot product to ensure it's within the valid range for acos [-1, 1]
		dotProduct = MathHelper.clamp(dotProduct, 0.0, 1.0);

		// Calculate the angle between the vectors
		double angleBetween = Math.acos(dotProduct);

		// If the angle is very small, just return the current velocity (no need to slerp)
		if(Math.abs(angleBetween) < MathHelper.EPSILON || MathHelper.approximatelyEquals(angleBetween, MathHelper.PI))
			return new Vector2d(currentVelocity);

		// Calculate the fraction of the way we want to rotate (clamp to 0.0 to 1.0)
		double t = Math.min(1.0, turnSpeedRadians / angleBetween);

		// Slerp calculation
		double sinTotal = Math.sin(angleBetween);
		double ratioA = Math.sin((1 - t) * angleBetween) / sinTotal;
		double ratioB = Math.sin(t * angleBetween) / sinTotal;

		// Compute the new direction as a weighted sum of the two directions
		Vector2d newDir = new Vector2d(
				currentDir.x * ratioA + intendedDir.x * ratioB,
				currentDir.y * ratioA + intendedDir.y * ratioB
		);

		// Maintain the original magnitude of the velocity
		newDir.mul(currentVelocity.length());

		return newDir; // Return the interpolated direction with original magnitude
	}

	private final MarioTimers TIMERS = new MarioTimers();
	@Override public @NotNull MarioTimers getTimers() {
		return this.TIMERS;
	}

	public WallboundActionDefinition.WallInfo getWallInfo() {
		return null;
	}

	public abstract boolean travelHook(double forwardInput, double strafeInput);
}
