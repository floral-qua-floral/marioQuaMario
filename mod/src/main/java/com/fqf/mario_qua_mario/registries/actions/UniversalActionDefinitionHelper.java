package com.fqf.mario_qua_mario.registries.actions;

import com.fqf.mario_qua_mario.mariodata.MarioServerPlayerData;
import com.fqf.mario_qua_mario_api.definitions.states.actions.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario_api.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.mariodata.MarioMoveableData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_api.util.StatCategory;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

public class UniversalActionDefinitionHelper implements
		GroundedActionDefinition.GroundedActionHelper,
		AirborneActionDefinition.AirborneActionHelper,
		AquaticActionDefinition.AquaticActionHelper,
		WallboundActionDefinition.WallboundActionHelper,
		MountedActionDefinition.MountedActionHelper,
		TransitionInjectionDefinition.TransitionCreator.CastableHelper {
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
		return Math.pow(0.6 / getFloorSlipperiness(data.getMario()), 3);
	}
	private static float getFloorSlipperiness(Entity stepper) {
		if(stepper.isOnGround()) {
			BlockPos blockPos = stepper.getVelocityAffectingPos();
			return stepper.getWorld().getBlockState(blockPos).getBlock().getSlipperiness();
		}
		return 0.6F;
	}

	@Override
	public void performJump(IMarioTravelData data, CharaStat jumpVel, @Nullable CharaStat speedAddend) {
		double newYVel = jumpVel.get(data);
		if(speedAddend != null) newYVel += speedAddend.get(data) * getSpeedFactor(data);
		data.setYVel(newYVel);
	}
	private double getSpeedFactor(IMarioTravelData data) {
		double scaledForwardVel = data.getForwardVel();
		if(scaledForwardVel < 0) return scaledForwardVel * 0.2;
		if(scaledForwardVel < 1) return scaledForwardVel * scaledForwardVel;
		return scaledForwardVel;
	}

	@Override public void applyComplexGravity(
			IMarioTravelData data,
			CharaStat gravity, @Nullable CharaStat jumpingGravity,
			CharaStat terminalVelocity
	) {
		this.applyGravity(data, (jumpingGravity == null || ((MarioMoveableData) data).jumpCapped) ? gravity : jumpingGravity, terminalVelocity);
	}

	@Override public void airborneAccel(
			IMarioTravelData data,
			CharaStat forwardAccelStat, CharaStat forwardSpeedStat,
			CharaStat backwardAccelStat, CharaStat backwardSpeedStat,
			CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
	) {
		boolean forwards = data.getInputs().getForwardInput() >= 0;
		driftingAccel(data,
				forwards ? forwardAccelStat : backwardAccelStat,
				forwards ? forwardSpeedStat : backwardSpeedStat,
				strafeAccelStat, strafeSpeedStat,
				forwardAngleContribution, strafeAngleContribution, redirectStat
		);
	}

	private void driftingAccel(
			IMarioTravelData data,
			CharaStat accelStat, CharaStat speedStat,
			CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
	) {
		double forwardInput = data.getInputs().getForwardInput();
		double strafeInput = data.getInputs().getStrafeInput();
		double forwardVel = data.getForwardVel();
		double strafeVel = data.getStrafeVel();

		double accelValue, strafeAccelValue;

		if(forwardInput != 0 && (Math.signum(forwardVel) != Math.signum(forwardInput) || Math.abs(forwardVel) < Math.abs(speedStat.get(data))))
			accelValue = accelStat.get(data) * forwardInput;
		else accelValue = 0;

		if(strafeInput != 0 && (Math.signum(strafeVel) != Math.signum(strafeInput) || Math.abs(strafeVel) < Math.abs(strafeSpeedStat.get(data))))
			strafeAccelValue = strafeAccelStat.get(data) * strafeInput;
		else strafeAccelValue = 0;

		data.approachAngleAndAccel(
				accelValue, speedStat.get(data) * Math.signum(forwardInput),
				strafeAccelValue, strafeSpeedStat.get(data) * Math.signum(strafeInput),
				forwardAngleContribution, strafeAngleContribution, redirectStat.get(data)
		);
	}

	@Override
	public TransitionDefinition makeJumpCapTransition(IncompleteActionDefinition forAction, double capThreshold) {
		CharaStat cap = new CharaStat(capThreshold, StatCategory.JUMP_CAP);
		return new TransitionDefinition(
				forAction.getID(),
				data -> !((MarioMoveableData) data).jumpCapped && (!data.getInputs().JUMP.isHeld()  || data.getYVel() < cap.get(data)),
				EvaluatorEnvironment.CLIENT_ONLY,
				data -> {
					((MarioMoveableData) data).jumpCapped = true;
					data.setYVel(Math.min(cap.get(data), data.getYVel()));
				},
				(data, isSelf, seed) -> data.fadeJumpSound()
		);
	}

	@Override
	public void applyGravity(IMarioTravelData data, CharaStat gravity, CharaStat terminalVelocity) {
		double maxFallSpeed = terminalVelocity.get(data);
		double yVel = data.getYVel();
		if(yVel > maxFallSpeed) {
			yVel += gravity.get(data);
			data.setYVel(Math.max(maxFallSpeed, yVel));
		}
	}

	@Override
	public void applyWaterDrag(IMarioTravelData data, CharaStat drag, CharaStat dragMin) {
		double dragValue = drag.get(data);
		boolean dragInverted = dragValue < 0;
		double slipFactor = 1.0;
		double dragMinValue = dragMin.get(data) * slipFactor;
		if(!dragInverted) dragValue *= slipFactor;


		Vec3d deltaVelocities = new Vec3d(
				-dragValue * data.getForwardVel(),
				-dragValue * data.getYVel(),
				-dragValue * data.getStrafeVel()
		);
		double dragVelocitySquared = deltaVelocities.lengthSquared();
		if(dragVelocitySquared != 0 && dragVelocitySquared < dragMinValue * dragMinValue)
			deltaVelocities = deltaVelocities.normalize().multiply(dragMinValue);

		data.setForwardVel(data.getForwardVel() + deltaVelocities.x);
		data.setYVel(data.getYVel() + deltaVelocities.y);
		data.setStrafeVel(data.getStrafeVel() + deltaVelocities.z);
	}

	@Override
	public void aquaticAccel(
			IMarioTravelData data,
			CharaStat forwardAccelStat, CharaStat forwardSpeedStat,
			CharaStat backwardAccelStat, CharaStat backwardSpeedStat,
			CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
	) {
		boolean forwards = data.getInputs().getForwardInput() >= 0;
		driftingAccel(data,
				forwards ? forwardAccelStat : backwardAccelStat,
				forwards ? forwardSpeedStat : backwardSpeedStat,
				strafeAccelStat, strafeSpeedStat,
				forwardAngleContribution, strafeAngleContribution, redirectStat
		);
	}

	@Override
	public WallboundActionDefinition.WallInfo getWallInfo(IMarioReadableMotionData data) {
		return ((MarioMoveableData) data).getWallInfo();
	}

	@Override
	public float getAngleDifference(float alfa, float bravo) {
		return MathHelper.subtractAngles(alfa, bravo);
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

	@Override
	public Entity getMount(IMarioReadableMotionData data) {
		return data.getMario().getVehicle();
	}


	@Override
	public void dismount(IMarioTravelData data, boolean reposition) {
		data.getMario().stopRiding();
		if(!reposition && data instanceof MarioServerPlayerData serverData)
			serverData.cancelNextRequestTeleportPacket = true;
//		((MarioPlayerData) data).attemptDismount = reposition
//				? MarioPlayerData.DismountType.VANILLA_DISMOUNT
//				: MarioPlayerData.DismountType.DISMOUNT_IN_PLACE;
	}

	@Override
	public double getSlipFactor(Entity mount) {
		return mount.isOnGround() ? 1.0 : Math.pow(0.6 / getFloorSlipperiness(mount), 3);
	}

	@Override public GroundedActionDefinition.GroundedActionHelper asGrounded() {
		return this;
	}
	@Override public AirborneActionDefinition.AirborneActionHelper asAirborne() {
		return this;
	}
	@Override public AquaticActionDefinition.AquaticActionHelper asAquatic() {
		return this;
	}
	@Override public WallboundActionDefinition.WallboundActionHelper asWallbound() {
		return this;
	}
	@Override public MountedActionDefinition.MountedActionHelper asMounted() {
		return this;
	}
}
