package com.fqf.mario_qua_mario_api.definitions.states.actions;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface WallboundActionDefinition extends IncompleteActionDefinition {
	void travelHook(IMarioTravelData data, @Nullable WallInfo wall, WallboundActionHelper helper);

	@NotNull List<TransitionDefinition> getBasicTransitions(WallboundActionHelper helper);
	@NotNull List<TransitionDefinition> getInputTransitions(WallboundActionHelper helper);
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions(WallboundActionHelper helper);

	/**
	 * Provides some information about the wall Mario is interacting with and his relationship to it.
	 */
	interface WallInfo {
		Vec3d getWallNormal();
		float getWallYaw();

		double getTowardsWallInput();
		double getSidleInput();

		double getTowardsWallVel();
		double getSidleVel();
	}

	/**
	 * Contains a number of methods intended to help with the creation of Wallbound Actions.
	 */
	interface WallboundActionHelper {
		void assignWallDirection(IMarioTravelData data, Direction direction);
		@Nullable WallInfo getWallInfo(IMarioReadableMotionData data);

		float getAngleDifference(float alfa, float bravo);

		void applyGravity(IMarioTravelData data, CharaStat gravity, CharaStat terminalVelocity);

		void climbWall(
				IMarioTravelData data,
				CharaStat ascendSpeedStat, CharaStat ascendAccelStat,
				CharaStat descendSpeedStat, CharaStat descendAccelStat,
				CharaStat sidleSpeedStat, CharaStat sidleAccelStat
		);

		void setSidleVel(IMarioTravelData data, double sidleVel);
	}
}
