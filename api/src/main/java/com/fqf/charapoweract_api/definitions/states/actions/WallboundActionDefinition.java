package com.fqf.charapoweract_api.definitions.states.actions;

import com.fqf.charapoweract_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.WallBodyAlignment;
import com.fqf.charapoweract_api.cpadata.ICPAReadableMotionData;
import com.fqf.charapoweract_api.cpadata.ICPATravelData;
import com.fqf.charapoweract_api.util.CharaStat;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public interface WallboundActionDefinition extends IncompleteActionDefinition {
	@NotNull WallBodyAlignment getBodyAlignment();

	float getHeadYawRange();

	/**
	 * Called on the client the instant the player transitions into this action. The result is networked to the server
	 * and then to other clients without being verified.
	 * @param data
	 * @return
	 */
	float getWallYaw(ICPAReadableMotionData data);

	/**
	 * Called on the server to validate whether transitioning into this action is allowed. This is never called if
	 * the gamerule cpaRejectIllegalActionTransitions is set to false!
	 *
	 * @param data        Player's data (server-sided)
	 * @param wall        Information about the wall, as claimed by the client.
	 * @param checkOffset
	 * @return Whether the transition is allowed. If false, it is rejected and the player is forced back into her previous
	 * action.
	 */
	boolean checkLegality(ICPAReadableMotionData data, WallInfo wall, Vec3d checkOffset);

	void travelHook(ICPATravelData data, WallInfo wall, WallboundActionHelper helper);

	@NotNull List<TransitionDefinition> getBasicTransitions(WallboundActionHelper helper);
	@NotNull List<TransitionDefinition> getInputTransitions(WallboundActionHelper helper);
	@NotNull List<TransitionDefinition> getWorldCollisionTransitions(WallboundActionHelper helper);

	/**
	 * Provides some information about the wall the player is interacting with and her relationship to it.
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

		boolean isLegal();
		boolean wouldBeLegalWithOffset(double yOffset, double sidleOffset);
	}

	/**
	 * Contains a number of methods intended to help with the creation of Wallbound Actions.
	 */
	interface WallboundActionHelper {
		WallInfo getWallInfo(ICPAReadableMotionData data);

		float getAngleDifference(float alfa, float bravo);

		void applyGravity(ICPATravelData data, CharaStat gravity, CharaStat terminalVelocity);

		void climbWall(
				ICPATravelData data,
				CharaStat ascendSpeedStat, CharaStat ascendAccelStat,
				CharaStat descendSpeedStat, CharaStat descendAccelStat,
				CharaStat sidleSpeedStat, CharaStat sidleAccelStat
		);

		void setSidleVel(ICPATravelData data, double sidleVel);

		void setTowardsWallVel(ICPATravelData data, double towardsWallVel);
	}
}
