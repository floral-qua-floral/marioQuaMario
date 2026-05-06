package com.fqf.charaformact_api.definitions.states.actions;

import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.WallBodyAlignment;
import com.fqf.charaformact_api.util.CfaStat;
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
	float getWallYaw(CfaReadableMotionData data);

	/**
	 * Called on the server to validate whether transitioning into this action is allowed. This is never called if
	 * the gamerule cfaRejectIllegalActionTransitions is set to false!
	 *
	 * @param data        Player's data (server-sided)
	 * @param wall        Information about the wall, as claimed by the client.
	 * @param checkOffset
	 * @return Whether the transition is allowed. If false, it is rejected and the player is forced back into her previous
	 * action.
	 */
	boolean checkLegality(CfaReadableMotionData data, WallInfo wall, Vec3d checkOffset);

	void travelHook(CfaTravelData data, WallInfo wall, WallboundActionHelper helper);

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
		WallInfo getWallInfo(CfaReadableMotionData data);

		float getAngleDifference(float alfa, float bravo);

		void applyGravity(CfaTravelData data, CfaStat gravity, CfaStat terminalVelocity);

		void climbWall(
				CfaTravelData data,
				CfaStat ascendSpeedStat, CfaStat ascendAccelStat,
				CfaStat descendSpeedStat, CfaStat descendAccelStat,
				CfaStat sidleSpeedStat, CfaStat sidleAccelStat
		);

		void setSidleVel(CfaTravelData data, double sidleVel);

		void setTowardsWallVel(CfaTravelData data, double towardsWallVel);
	}
}
