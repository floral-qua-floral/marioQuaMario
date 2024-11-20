package com.floralquafloral.definitions.actions;

import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioTravelData;
import com.floralquafloral.util.MarioSFX;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

import static com.floralquafloral.definitions.actions.StatCategory.*;

public abstract class GroundedActionDefinition implements ActionDefinition {
	public static final CharaStat ZERO = new CharaStat(0.0);

	public abstract static class GroundedTransitions {
		public static final CharaStat P_SPEED = new CharaStat(0.665, P_RUNNING, FORWARD, SPEED);
		public static void performJump(MarioTravelData data, CharaStat velocityStat, @Nullable CharaStat addendStat) {
			if(data.getMario().isMainPlayer() || !data.getMario().getWorld().isClient) {
				double jumpVel = velocityStat.get(data);
				if(addendStat != null)
					jumpVel += Math.max(0.0, data.getForwardVel() / P_SPEED.get(data)) * addendStat.get(data);

				data.setYVel(jumpVel);
			}
		}

		public static final ActionTransitionDefinition FALL = new ActionTransitionDefinition(
				"qua_mario:fall",
				(data) -> !data.getMario().isOnGround()
		);

		public static final CharaStat JUMP_ADDEND = new CharaStat(0.117, StatCategory.JUMP_VELOCITY);
		public static final CharaStat JUMP_VEL = new CharaStat(0.858, StatCategory.JUMP_VELOCITY);
		public static final ActionTransitionDefinition JUMP = new ActionTransitionDefinition(
				"qua_mario:jump",
				(data) -> data.getInputs().JUMP.isPressed(),
				data -> performJump(data, JUMP_VEL, JUMP_ADDEND),
				(data, isSelf, seed) -> data.playJumpSound(seed)
		);

		public static final ActionTransitionDefinition DUCK_WADDLE = new ActionTransitionDefinition(
				"qua_mario:duck_waddle",
				(data) -> data.getInputs().DUCK.isHeld(),
				null,
				(data, isSelf, seed) -> {
					data.playSoundEvent(MarioSFX.DUCK, 1.0F, 0.5F, seed);
					data.voice(MarioClientSideData.VoiceLine.DUCK, seed);
				}
		);
	}

	@Override public final void travelHook(MarioTravelData data) {
		data.getTimers().jumpCapped = false;
		if(data.isClient())
			data.setYVel(data.getYVel() + AirborneActionDefinition.AerialStats.GRAVITY.get(data));
		else
			data.setYVel(-0.1);

		this.groundedTravel(data);
	}

	public abstract void groundedTravel(MarioTravelData data);

	public void groundAccel(
			MarioTravelData data,
			CharaStat forwardAccel, CharaStat forwardTarget, CharaStat strafeAccel, CharaStat strafeTarget,
			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectDelta
	) {
		double slipFactor = getSlipFactor(data);

		data.approachAngleAndAccel(
				forwardAccel.get(data) * slipFactor, forwardTarget.get(data) * data.getInputs().getForwardInput(),
				strafeAccel.get(data) * slipFactor, strafeTarget.get(data) * data.getInputs().getStrafeInput(),
				forwardAngleContribution, strafeAngleContribution, redirectDelta.get(data) * slipFactor
		);
	}

	public void applyDrag(
			MarioTravelData data,
			CharaStat drag, CharaStat dragMin,
			double forwardAngleContribution, double strafeAngleContribution,
			CharaStat redirection
	) {
		double dragValue = drag.get(data);
		boolean dragInverted = dragValue < 0;
		double slipFactor = getSlipFactor(data);
		double dragMinValue = dragMin.get(data) * slipFactor;
		if(!dragInverted) dragValue *= slipFactor;


		Vector2d deltaVelocities = new Vector2d(
				-dragValue * data.getForwardVel(),
				-dragValue * data.getStrafeVel()
		);
		double dragVelocitySquared = deltaVelocities.lengthSquared();
		if(dragVelocitySquared != 0 && dragVelocitySquared < dragMinValue * dragMinValue)
			deltaVelocities.normalize(dragMinValue);

		if(dragInverted) {
			data.setForwardStrafeVel(data.getForwardVel() + deltaVelocities.x, data.getStrafeVel() + deltaVelocities.y);
		}
		else {
			data.approachAngleAndAccel(
					deltaVelocities.x, 0,
					deltaVelocities.y, 0,
					forwardAngleContribution,
					strafeAngleContribution,
					redirection.get(data) * slipFactor
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
