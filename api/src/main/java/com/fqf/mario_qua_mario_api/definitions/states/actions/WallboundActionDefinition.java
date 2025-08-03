package com.fqf.mario_qua_mario_api.definitions.states.actions;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.WallBodyAlignment;
import com.fqf.mario_qua_mario_api.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public interface WallboundActionDefinition extends IncompleteActionDefinition {
	@NotNull WallBodyAlignment getBodyAlignment();

	float getHeadYawRange();

	/**
	 * Called on the client the instant Mario transitions into this action. The result is networked to the server
	 * and then to other clients without checking.
	 * @param data
	 * @return
	 */
	float getWallYaw(IMarioReadableMotionData data);

	/**
	 * Called on the server to validate whether transitioning into this action is allowed. This is never called if
	 * the gamerule mqmRejectIllegalActionTransitions is set to false!
	 *
	 * @param data Mario's data (server-sided)
	 * @param wall Information about the wall, as claimed by the client.
	 * @return Whether the transition is allowed. If false, it is rejected and Mario is forced back into his previous
	 * action.
	 */
	boolean checkServerSidedLegality(IMarioReadableMotionData data, WallInfo wall);

	void travelHook(IMarioTravelData data, WallInfo wall, WallboundActionHelper helper);

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

		float getYawDeviation();
		double getDistanceFromWall(double maxDistance);
		Set<BlockPos> getWallBlocks(double maxDistance);
	}

	/**
	 * Contains a number of methods intended to help with the creation of Wallbound Actions.
	 */
	interface WallboundActionHelper {
		WallInfo getWallInfo(IMarioReadableMotionData data);

		float getAngleDifference(float alfa, float bravo);

		void applyGravity(IMarioTravelData data, CharaStat gravity, CharaStat terminalVelocity);

		void climbWall(
				IMarioTravelData data,
				CharaStat ascendSpeedStat, CharaStat ascendAccelStat,
				CharaStat descendSpeedStat, CharaStat descendAccelStat,
				CharaStat sidleSpeedStat, CharaStat sidleAccelStat
		);

		void setSidleVel(IMarioTravelData data, double sidleVel);

		void setTowardsWallVel(IMarioTravelData data, double towardsWallVel);
	}
}
