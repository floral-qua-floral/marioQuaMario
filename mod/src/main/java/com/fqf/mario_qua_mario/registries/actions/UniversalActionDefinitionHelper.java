package com.fqf.mario_qua_mario.registries.actions;

import com.fqf.mario_qua_mario.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.AquaticActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.WallboundActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario.util.CharaStat;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

public class UniversalActionDefinitionHelper implements
		GroundedActionDefinition.GroundedActionHelper,
		AirborneActionDefinition.AirborneActionHelper,
		AquaticActionDefinition.AquaticActionHelper,
		WallboundActionDefinition.WallboundActionHelper {
	public static final UniversalActionDefinitionHelper INSTANCE = new UniversalActionDefinitionHelper();
	private UniversalActionDefinitionHelper() {}

	@Override
	public void groundAccel(
			IMarioTravelData data,
			CharaStat forwardAccelStat, CharaStat forwardSpeedStat,
			CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
	) {
		double slipFactor = getSlipFactor(data);

		data.approachAngleAndAccel(
				forwardAccelStat.get(data) * slipFactor, forwardSpeedStat.get(data) * data.getInputs().getForwardInput(),
				strafeAccelStat.get(data) * slipFactor, strafeSpeedStat.get(data) * data.getInputs().getStrafeInput(),
				forwardAngleContribution, strafeAngleContribution, redirectStat.get(data) * slipFactor
		);
	}

	@Override
	public void applyDrag(
			IMarioTravelData data,
			CharaStat drag, CharaStat dragMin,
			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirection
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

	@Override
	public double getSlipFactor(IMarioReadableMotionData data) {
		return Math.pow(0.6 / getFloorSlipperiness(data), 3);
	}
	private static float getFloorSlipperiness(IMarioReadableMotionData data) {
		if(data.getMario().isOnGround()) {
			BlockPos blockPos = data.getMario().getVelocityAffectingPos();
			return data.getMario().getWorld().getBlockState(blockPos).getBlock().getSlipperiness();
		}
		return 0.6F;
	}

	@Override public void applyGravity(
			IMarioTravelData data,
			CharaStat gravity, @Nullable CharaStat jumpingGravity,
			CharaStat terminalVelocity
	) {

	}

	@Override public void airborneAccel(
			IMarioTravelData data,
			CharaStat forwardAccelStat, CharaStat forwardSpeedStat,
			CharaStat backwardAccelStat, CharaStat backwardSpeedStat,
			CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
	) {

	}

	@Override
	public TransitionDefinition makeJumpCapTransition(IncompleteActionDefinition forAction, double capThreshold) {
		return null;
	}

	@Override
	public void applyGravity(IMarioTravelData data, CharaStat gravity, CharaStat terminalVelocity) {

	}

	@Override
	public void applyWaterDrag(IMarioTravelData data, CharaStat drag, CharaStat dragMin) {

	}

	@Override
	public void aquaticAccel(
			IMarioTravelData data,
			CharaStat forwardAccelStat, CharaStat forwardSpeedStat,
			CharaStat backwardAccelStat, CharaStat backwardSpeedStat,
			CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
	) {

	}

	@Override
	public WallboundActionDefinition.WallInfo getWallInfo(IMarioReadableMotionData data) {
		return ((MarioMoveableData) data).getWallInfo();
	}

	@Override public void climbWall(
			IMarioTravelData data,
			CharaStat ascendSpeedStat,CharaStat ascendAccelStat,
			CharaStat descendSpeedStat, CharaStat descendAccelStat,
			CharaStat sidleSpeedStat, CharaStat sidleAccelStat
	) {

	}

	@Override
	public void setSidleVel(IMarioTravelData data, double sidleVel) {

	}
}
