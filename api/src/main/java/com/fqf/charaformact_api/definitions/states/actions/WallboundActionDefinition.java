package com.fqf.charaformact_api.definitions.states.actions;

import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.IncompleteActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.WallBodyAlignment;
import com.fqf.charaformact_api.util.CfaStat;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface WallboundActionDefinition extends IncompleteActionDefinition {
	@NotNull WallBodyAlignment defineBodyAlignment();

	float defineHeadYawRange();

	/**
	 * Called on the client the instant the player transitions into this action. The result is networked to the server
	 * and then to other clients without being verified. The transition itself is then verified by verifyLegality.
	 */
	float calculateWallYaw(CfaReadableMotionData data);

	/**
	 * Called on the server to validate whether transitioning into this action is allowed. This is also called by
	 * WallInfo.isLegal and WallInfo.wouldBeLegalWithOffset.
	 *
	 * @param data        Player's data
	 * @param wall        Information about the wall, as claimed by the client.
	 * @param checkOffset If you need to use the player's position directly, please offset it by checkOffset to ensure
	 *                    that all WallInfo methods will work as intended.
	 * @return Whether the transition is allowed. If false, it is rejected and the player is forced back into her
	 * previous action.
	 */
	boolean verifyLegality(CfaReadableMotionData data, WallInfo wall, Vec3d checkOffset);

	void travel(CfaTravelData data, WallInfo wall, WallboundActionHelper helper);

	default void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, WallboundActionHelper helper) {

	}
	default void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, WallboundActionHelper helper) {

	}
	default void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, WallboundActionHelper helper) {

	}

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
