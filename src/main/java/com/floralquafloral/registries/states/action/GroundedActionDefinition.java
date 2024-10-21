package com.floralquafloral.registries.states.action;

import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.client.Input;
import com.floralquafloral.mariodata.client.MarioClientData;
import com.floralquafloral.util.MarioSFX;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector2d;

import static com.floralquafloral.MarioQuaMario.LOGGER;

public abstract class GroundedActionDefinition implements ActionDefinition {
	protected abstract static class GroundedTransitions {
		public static final ActionTransitionDefinition FALL = new ActionTransitionDefinition(
				"qua_mario:fall",
				(data) -> !data.getMario().isOnGround()
		);

		public static final ActionTransitionDefinition JUMP = new ActionTransitionDefinition(
				"qua_mario:jump",
				(data) -> Input.DUCK.isHeld()
//					double threshold = DUCK_SLIDE_THRESHOLD.get(data);
//					return Input.DUCK.isHeld() && Vector2d.lengthSquared(data.getForwardVel(), data.getStrafeVel()) > threshold * threshold;
		);

		public static final ActionTransitionDefinition DUCK_WADDLE = new ActionTransitionDefinition(
				"qua_mario:duck_waddle",
				(data) -> Input.DUCK.isHeld(),
				(data, isSelf, seed) -> {
					// Play duck voiceline
					data.getMario().playSound(MarioSFX.DUCK);
					LOGGER.info("Ducking voiceline with seed {}", seed);
				},
				(data, seed) -> {
					LOGGER.info("Entering duck_waddle on server with seed {}", seed);
				}
		);
	}

	@Override public void selfTick(MarioClientData data) {
		data.setYVel(data.getYVel() - 0.05);
		this.groundedSelfTick(data);
	}

	public abstract void groundedSelfTick(MarioClientData data);

	public void groundAccel(
			MarioClientData data,
			double forwardAccel, double forwardTarget, double strafeAccel, double strafeTarget,
			double forwardAngleContribution, double strafeAngleContribution, double redirectDelta
	) {
		double slipFactor = getSlipFactor(data);
		data.approachAngleAndAccel(
				forwardAccel * slipFactor, forwardTarget * Input.getForwardInput(),
				strafeAccel * slipFactor, strafeTarget * Input.getStrafeInput(),
				forwardAngleContribution, strafeAngleContribution, redirectDelta * slipFactor
		);
	}

	public void applyDrag(
			MarioClientData data,
			double drag, double dragMin,
			double forwardAngleContribution, double strafeAngleContribution,
			double redirection
	) {
		boolean dragInverted = drag < 0;
		double slipFactor = getSlipFactor(data);
		dragMin *= slipFactor;
		if(!dragInverted) drag *= slipFactor;


		Vector2d deltaVelocities = new Vector2d(
				-drag * data.getForwardVel(),
				-drag * data.getStrafeVel()
		);
		double dragVelocitySquared = deltaVelocities.lengthSquared();
		if(dragVelocitySquared != 0 && dragVelocitySquared < dragMin * dragMin)
			deltaVelocities.normalize(dragMin);

		if(dragInverted) {
			data.setForwardStrafeVel(data.getForwardVel() + deltaVelocities.x, data.getStrafeVel() + deltaVelocities.y);
		}
		else {
			data.approachAngleAndAccel(
					deltaVelocities.x, 0,
					deltaVelocities.y, 0,
					forwardAngleContribution,
					strafeAngleContribution,
					redirection
			);
		}
	}

	public static double getSlipFactor(MarioData data) {
		return Math.pow(0.6 / getFloorSlipperiness(data), 3);
	}
	private static float getFloorSlipperiness(MarioData data) {
		if(data.getMario().isOnGround()) {
			BlockPos blockPos = data.getMario().getVelocityAffectingPos();
			return data.getMario().getWorld().getBlockState(blockPos).getBlock().getSlipperiness();
		}
		return 0.6F;
	}
}
