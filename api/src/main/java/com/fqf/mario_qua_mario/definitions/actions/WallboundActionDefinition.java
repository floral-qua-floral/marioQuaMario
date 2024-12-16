package com.fqf.mario_qua_mario.definitions.actions;

import com.fqf.mario_qua_mario.definitions.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario.definitions.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.CharaStat;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WallboundActionDefinition extends IncompleteActionDefinition {
	void travelHook(IMarioTravelData data, WallInfo wall, WallboundActionHelper helper);

	@NotNull List<TransitionDefinition> getBasicTransitions(WallboundActionHelper helper);
	@NotNull List<TransitionDefinition> getInputTransitions(WallboundActionHelper helper);
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions(WallboundActionHelper helper);

	/**
	 * Provides some information about the wall Mario is interacting with and his relationship to it.
	 */
	interface WallInfo {
		Vec3d getWallNormal();
		double getNormalYaw();

		double getTowardsWallInput();
		double getSidleInput();

		double getSidleVel();
	}

	/**
	 * Contains a number of methods intended to help with the creation of Wallbound Actions.
	 */
	interface WallboundActionHelper {
		WallInfo getWallInfo(IMarioReadableMotionData data);

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
