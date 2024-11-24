package com.floralquafloral.definitions.actions;

import com.floralquafloral.mariodata.MarioTravelData;
import net.minecraft.entity.EntityPose;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

import static com.floralquafloral.definitions.actions.StatCategory.*;

public abstract class AquaticActionDefinition implements ActionDefinition {
	public abstract static class AquaticTransitions {
		public static final ActionTransitionDefinition EXIT_WATER = new ActionTransitionDefinition(
				"qua_mario:fall",
				data ->
						data.getMario().getFluidHeight(FluidTags.WATER)
						< data.getMario().getBoundingBox(EntityPose.STANDING).getLengthY() * 0.5
		);

		public static final ActionTransitionDefinition AQUATIC_GROUND_POUND = new ActionTransitionDefinition(
				"qua_mario:aquatic_ground_pound_windup",
				AirborneActionDefinition.AerialTransitions.GROUND_POUND.EVALUATOR,
				AirborneActionDefinition.AerialTransitions.GROUND_POUND.EXECUTOR_TRAVELLERS,
				AirborneActionDefinition.AerialTransitions.GROUND_POUND.EXECUTOR_CLIENTS
		);

		public static ActionTransitionDefinition FALL = new ActionTransitionDefinition("qua_mario:submerged",
				GroundedActionDefinition.GroundedTransitions.FALL.EVALUATOR,
				GroundedActionDefinition.GroundedTransitions.FALL.EXECUTOR_TRAVELLERS,
				GroundedActionDefinition.GroundedTransitions.FALL.EXECUTOR_CLIENTS
		);

		public static final ActionTransitionDefinition LANDING = new ActionTransitionDefinition(
				"qua_mario:underwater_walk",
				AirborneActionDefinition.AerialTransitions.BASIC_LANDING.EVALUATOR
		);
	}

	public abstract static class AquaticStats {
//		public final CharaStat GRAVITY = new CharaStat(0, StatCategory.AQUATIC_GRAVITY);
//		public final CharaStat TERMINAL_VELOCITY = new CharaStat(0, StatCategory.AQUATIC_TERMINAL_VELOCITY);
//
//		public final CharaStat DRAG = new CharaStat(0.07, StatCategory.WATER_DRAG);
//		public final CharaStat DRAG_MIN = new CharaStat(0.01, StatCategory.WATER_DRAG);

		public static final CharaStat FORWARD_SWIM_ACCEL = new CharaStat(0.025, SWIMMING, FORWARD, ACCELERATION);
		public static final CharaStat FORWARD_SWIM_SPEED = new CharaStat(0.25, SWIMMING, FORWARD, SPEED);

		public static final CharaStat BACKWARD_SWIM_ACCEL = new CharaStat(0.035, SWIMMING, BACKWARD, ACCELERATION);
		public static final CharaStat BACKWARD_SWIM_SPEED = new CharaStat(0.2, SWIMMING, BACKWARD, SPEED);

		public static final CharaStat STRAFE_SWIM_ACCEL = new CharaStat(0.025, SWIMMING, STRAFE, ACCELERATION);
		public static final CharaStat STRAFE_SWIM_SPEED = new CharaStat(0.25, SWIMMING, STRAFE, SPEED);

		public static final CharaStat SWIM_REDIRECTION = new CharaStat(2.0, SWIMMING, REDIRECTION);
	}

	private final @Nullable CharaStat ACTION_GRAVITY = getGravity() == 0 ? null : new CharaStat(getGravity(), StatCategory.AQUATIC_GRAVITY);
	private final @Nullable CharaStat ACTION_TERMINAL_VELOCITY = getGravity() == 0 ? null : new CharaStat(getTerminalVelocity(), StatCategory.AQUATIC_TERMINAL_VELOCITY);
	private final @Nullable CharaStat ACTION_DRAG = getDrag() == 0 ? null : new CharaStat(getDrag(), StatCategory.WATER_DRAG);
	private final @Nullable CharaStat ACTION_DRAG_MINIMUM = getDrag() == 0 ? null : new CharaStat(getDragMinimum(), StatCategory.WATER_DRAG);

	public abstract double getGravity();
	public abstract double getTerminalVelocity();
	public abstract double getDrag();
	public abstract double getDragMinimum();

	@Override
	public void travelHook(MarioTravelData data) {
		data.getTimers().jumpCapped = false;
		data.getMario().fallDistance = 0;
		if(ACTION_TERMINAL_VELOCITY != null) {
			double yVel = data.getYVel();
			double terminalVelocity = ACTION_TERMINAL_VELOCITY.get(data);

			if(yVel > terminalVelocity) {
				assert ACTION_GRAVITY != null;
				yVel += ACTION_GRAVITY.get(data);

				data.setYVel(Math.max(terminalVelocity, yVel));
			}
		}
		if(ACTION_DRAG_MINIMUM != null) {
			assert ACTION_DRAG != null;
			applyAquaticDrag(data, ACTION_DRAG, ACTION_DRAG_MINIMUM);
		}

		aquaticTravel(data);
	}

	public abstract void aquaticTravel(MarioTravelData data);

	public static void aquaticAccel(
			MarioTravelData data,
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
				forwardAngleContribution * forwardInput,
				strafeAngleContribution * strafeInput,
				redirectStat.get(data)
		);
	}
	public static void aquaticAccel(
			MarioTravelData data,
			CharaStat forwardAccelStat, CharaStat forwardSpeedStat,
			CharaStat backwardAccelStat, CharaStat backwardSpeedStat,
			CharaStat strafeAccelStat, CharaStat strafeSpeedStat,
			double forwardAngleContribution, double strafeAngleContribution, CharaStat redirectStat
	) {
		// Underwater acceleration is extremely similar to airborne acceleration. However, underwater actions are
		// expected to have a great deal of drag, so Mario's speed will still approach zero.
		boolean forwards = data.getInputs().getForwardInput() >= 0;
		aquaticAccel(data,
				forwards ? forwardAccelStat : backwardAccelStat,
				forwards ? forwardSpeedStat : backwardSpeedStat,
				strafeAccelStat, strafeSpeedStat,
				forwardAngleContribution, strafeAngleContribution, redirectStat
		);
	}

	public static void applyAquaticDrag(MarioTravelData data, CharaStat drag, CharaStat dragMin) {
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
	public static void aquaticAccel(MarioTravelData data) {
		aquaticAccel(data,
				AquaticStats.FORWARD_SWIM_ACCEL, AquaticStats.FORWARD_SWIM_SPEED,
				AquaticStats.BACKWARD_SWIM_ACCEL, AquaticStats.BACKWARD_SWIM_SPEED,
				AquaticStats.STRAFE_SWIM_ACCEL, AquaticStats.STRAFE_SWIM_SPEED,
				1, 1, AquaticStats.SWIM_REDIRECTION
		);
	}
}
