package com.floralquafloral.mariodata.client;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.registries.states.action.ParsedAction;
import com.floralquafloral.registries.states.action.TransitionPhase;
import com.floralquafloral.util.CPMIntegration;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector2d;

public class MarioClientData extends MarioPlayerData {
	private static MarioClientData instance;
	public static MarioClientData getInstance() {
		return instance;
	}

	private ClientPlayerEntity marioClient;
	@Override public ClientPlayerEntity getMario() {
		return marioClient;
	}
	@Override public void setMario(PlayerEntity mario) {
		this.marioClient = (ClientPlayerEntity) mario;
		super.setMario(mario);
	}

	public MarioClientData(ClientPlayerEntity mario) {
		super(mario);
		this.marioClient = mario;
		MarioClientData.instance = this;
	}

	public int actionTimer = 0;
	@Override public void setAction(ParsedAction action, long seed) {
		MarioQuaMario.LOGGER.info("MarioClientData setAction to " + action.ID);
		if(action != getAction()) actionTimer = 0;
		if(this.getAction().ANIMATION != null)
			CPMIntegration.clientAPI.playAnimation(getAction().ANIMATION, 0);
		if(action.ANIMATION != null)
			CPMIntegration.clientAPI.playAnimation(action.ANIMATION, 1);
		super.setActionTransitionless(action);
	}

	public int jumpLandingTime = 0;
	public int doubleJumpLandingTime = 0;

	@Override public void tick() {
		Input.update(getMario());
	}

	public boolean travel(Vec3d movementInput) {
		Input.updateDirections(movementInput.z, movementInput.x);

		getAction().attemptTransitions(this, TransitionPhase.PRE_TICK);
		getAction().selfTick(this);
		getAction().attemptTransitions(this, TransitionPhase.POST_TICK);

		applyModifiedVelocity();
		marioClient.move(MovementType.PLAYER, marioClient.getVelocity());
		if(getAction().attemptTransitions(this, TransitionPhase.POST_MOVE))
			applyModifiedVelocity();

		marioClient.updateLimbs(false);
		return true;
	}

	public void approachAngleAndAccel(
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

			if (redirectDelta > 0) redirectedVel = slerp(currentVel, intendedAngle, redirectDelta);
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
		if(angleBetween < MathHelper.EPSILON || MathHelper.approximatelyEquals(angleBetween, MathHelper.PI))
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
}
