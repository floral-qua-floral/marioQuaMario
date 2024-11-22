package com.floralquafloral.definitions.actions;

import com.floralquafloral.mariodata.MarioTravelData;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;

public abstract class AquaticActionDefinition implements ActionDefinition {
	public abstract static class AquaticTransitions {
		public static final ActionTransitionDefinition EXIT_WATER = new ActionTransitionDefinition(
				"qua_mario:fall",
				data -> data.getInputs().SPIN.isPressed()
		);
	}

	public abstract static class AquaticStats {
		public final CharaStat GRAVITY = new CharaStat(0, StatCategory.AQUATIC_GRAVITY);
		public final CharaStat TERMINAL_VELOCITY = new CharaStat(0, StatCategory.AQUATIC_TERMINAL_VELOCITY);

		public final CharaStat DRAG = new CharaStat(0.07, StatCategory.WATER_DRAG);
		public final CharaStat DRAG_MIN = new CharaStat(0.01, StatCategory.WATER_DRAG);
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
	}

	public abstract void aquaticTravel(MarioTravelData data);

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
}
