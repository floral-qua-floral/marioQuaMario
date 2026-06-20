package com.fqf.charaformact.registries.actions;

import com.fqf.charaformact.cfadata.CfaMoveableData;
import com.fqf.charaformact.cfadata.CfaServerPlayerData;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact.cfadata.CfaPlayerData;
import com.fqf.charaformact.util.AdvancedWallInfo;
import com.fqf.charaformact_api.definitions.states.actions.*;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.StatCategory;
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
		GenericActionDefinition.CastableHelper {
	public static final UniversalActionDefinitionHelper INSTANCE = new UniversalActionDefinitionHelper();
	private UniversalActionDefinitionHelper() {}

	@Override
	public void groundAccel(
			CfaTravelData data,
			CfaStat forwardAccelStat, CfaStat forwardSpeedStat,
			CfaStat strafeAccelStat, CfaStat strafeSpeedStat,
			double forwardAngleContribution, double strafeAngleContribution, CfaStat redirectStat
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
			CfaTravelData data,
			CfaStat drag, CfaStat dragMin,
			double forwardAngleContribution, double strafeAngleContribution, CfaStat redirection
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
	public double getSlipFactor(CfaReadableMotionData data) {
		return Math.pow(0.6 / getFloorSlipperiness(data.getPlayer()), 3);
	}
	public static float getFloorSlipperiness(Entity stepper) {
		if(stepper.isOnGround()) {
			BlockPos blockPos = stepper.getVelocityAffectingPos();
			return stepper.getWorld().getBlockState(blockPos).getBlock().getSlipperiness();
		}
		return 0.6F;
	}

	@Override
	public void performJump(CfaTravelData data, CfaStat jumpVel, @Nullable CfaStat speedAddend) {
		double newYVel = jumpVel.get(data);
		if(speedAddend != null) newYVel += speedAddend.get(data) * getSpeedFactor(data);
		data.setYVel(newYVel);
	}
	private double getSpeedFactor(CfaTravelData data) {
		double scaledForwardVel = data.getForwardVel();
		if(scaledForwardVel < 0) return scaledForwardVel * 0.2;
		if(scaledForwardVel < 1) return scaledForwardVel * scaledForwardVel;
		return scaledForwardVel;
	}

	@Override public void applyComplexGravity(
			CfaTravelData data,
			CfaStat gravity, @Nullable CfaStat jumpingGravity,
			CfaStat terminalVelocity
	) {
		this.applyGravity(data, (jumpingGravity == null || ((CfaMoveableData) data).jumpCapped) ? gravity : jumpingGravity, terminalVelocity);
	}

	@Override public void airborneAccel(
			CfaTravelData data,
			CfaStat forwardAccelStat, CfaStat forwardSpeedStat,
			CfaStat backwardAccelStat, CfaStat backwardSpeedStat,
			CfaStat strafeAccelStat, CfaStat strafeSpeedStat,
			double forwardAngleContribution, double strafeAngleContribution, CfaStat redirectStat
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
			CfaTravelData data,
			CfaStat accelStat, CfaStat speedStat,
			CfaStat strafeAccelStat, CfaStat strafeSpeedStat,
			double forwardAngleContribution, double strafeAngleContribution, CfaStat redirectStat
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
		CfaStat cap = new CfaStat(capThreshold, StatCategory.JUMP_CAP);
		return new TransitionDefinition(
				forAction.defineID(),
				data -> !((CfaMoveableData) data).jumpCapped && (!data.getInputs().JUMP.isHeld()  || data.getYVel() < cap.get(data)),
				EvaluatorEnvironment.CLIENT_ONLY,
				data -> {
					((CfaMoveableData) data).jumpCapped = true;
					data.setYVel(Math.min(cap.get(data), data.getYVel()));
				},
				(data, isSelf, seed) -> data.fadeJumpSound()
		);
	}

	@Override
	public void applyGravity(CfaTravelData data, CfaStat gravity, CfaStat terminalVelocity) {
		double maxFallSpeed = terminalVelocity.get(data);
		double yVel = data.getYVel();
		if(yVel > maxFallSpeed) {
			yVel += gravity.get(data);
			data.setYVel(Math.max(maxFallSpeed, yVel));
		}
	}

	@Override
	public void applyWaterDrag(CfaTravelData data, CfaStat drag, CfaStat dragMin) {
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
			CfaTravelData data,
			CfaStat forwardAccelStat, CfaStat forwardSpeedStat,
			CfaStat backwardAccelStat, CfaStat backwardSpeedStat,
			CfaStat strafeAccelStat, CfaStat strafeSpeedStat,
			double forwardAngleContribution, double strafeAngleContribution, CfaStat redirectStat
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
	public AdvancedWallInfo getWallInfo(CfaReadableMotionData data) {
		return((CfaPlayerData) data).getWallInfo();
	}

	@Override
	public float getAngleDifference(float alfa, float bravo) {
		return MathHelper.subtractAngles(alfa, bravo);
	}

	@Override public void climbWall(
			CfaTravelData data,
			CfaStat ascendSpeedStat, CfaStat ascendAccelStat,
			CfaStat descendSpeedStat, CfaStat descendAccelStat,
			CfaStat sidleSpeedStat, CfaStat sidleAccelStat
	) {
		AdvancedWallInfo wall = this.getWallInfo(data);
		if(wall == null) return;
		double climbInput = wall.getTowardsWallInput();

		if(climbInput == 0) data.setYVel(0);
		else {
			double yVel = data.getYVel();
			double accel, target;
			if(climbInput > 0) {
				accel = ascendAccelStat.get(data);
				target = ascendSpeedStat.get(data) * Math.abs(climbInput);

				yVel = Math.min(yVel + accel, target);
			}
			else {
				accel = descendAccelStat.get(data);
				target = descendSpeedStat.get(data) * Math.abs(climbInput);

				yVel = Math.max(yVel + accel, target);

			}

			data.setYVel(yVel);
		}

		double sidleInput = wall.getSidleInput();
		if(sidleInput == 0) wall.setSidleVel(0);
		else {
			double sidleVel = wall.getSidleVel();

		}
	}

	@Override
	public void setSidleVel(CfaTravelData data, double sidleVel) {
		AdvancedWallInfo wall = this.getWallInfo(data);
		if(wall != null) wall.setSidleVel(sidleVel);
	}

	@Override
	public void setTowardsWallVel(CfaTravelData data, double towardsWallVel) {
		AdvancedWallInfo wall = this.getWallInfo(data);
		if(wall != null) wall.setTowardsWallVel(towardsWallVel);
	}

	@Override
	public Entity getMount(CfaReadableMotionData data) {
		return data.getPlayer().getVehicle();
	}

	@Override
	public void dismount(CfaTravelData data, boolean reposition) {
		if(!reposition && data instanceof CfaServerPlayerData serverData)
			serverData.skipDismountRepositioning();

		data.getPlayer().stopRiding();
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
