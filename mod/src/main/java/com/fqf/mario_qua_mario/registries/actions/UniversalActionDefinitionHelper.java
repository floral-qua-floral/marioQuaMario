package com.fqf.mario_qua_mario.registries.actions;

import com.fqf.mario_qua_mario.definitions.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario.definitions.actions.AquaticActionDefinition;
import com.fqf.mario_qua_mario.definitions.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario.definitions.actions.WallboundActionDefinition;
import com.fqf.mario_qua_mario.definitions.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario.definitions.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario.util.CharaStat;
import org.jetbrains.annotations.Nullable;

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

	}

	@Override
	public void applyDrag(
			IMarioTravelData data,
			CharaStat drag, CharaStat dragMin,
			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirection
	) {

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
