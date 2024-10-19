package com.floralquafloral.registries.action;

import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioPlayerData;
import com.floralquafloral.mariodata.client.Input;
import com.floralquafloral.mariodata.client.MarioClientData;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector2d;

import static com.floralquafloral.CharaStat.*;
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
				(data, isSelf) -> {
					// Play duck voiceline
					LOGGER.info("Ducking voiceline");
				},
				(data) -> {

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
//	public void groundAccel(
//			MarioClientData data,
//			CharaStat forwardAccel, CharaStat forwardTarget, CharaStat strafeAccel, CharaStat strafeTarget,
//			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectDelta
//	) {
//		groundAccel(data,
//				forwardAccel.get(data), forwardTarget.get(data),
//				strafeAccel.get(data), strafeTarget.get(data),
//				forwardAngleContribution, strafeAngleContribution, redirectDelta.get(data)
//		);
//	}

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
