package com.fqf.mario_qua_mario.actions.wallbound;

import com.fqf.charaformact_api.HelperGetter;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.ClimbTransitions;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class ClimbIntangibleDirectional extends ClimbWall implements WallboundActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("climb_intangible_directional");
	@Override public @NotNull Identifier defineID() {
		return ID;
	}

	@Override
	protected float getEntireBodyZOffset(CfaReadableMotionData data) {
		return data.retrieveStateData(ClimbOmniDirectionalVars.class).ALTERNATE_OFFSET ? -1.75F : 0;
	}

	private static float getYawOf(PlayerEntity mario, BlockPos pos, BlockState state) {
		return ClimbTransitions.yawOf(ClimbTransitions.getIntangibleClimbableDirectionality(mario, pos, state));
	}
	protected static float currentBlockYaw(CfaReadableMotionData data) {
		return getYawOf(data.getPlayer(), data.getPlayer().getBlockPos(), data.getPlayer().getBlockStateAtPos());
	}

	@Override
	public float calculateWallYaw(CfaReadableMotionData data) {
		return currentBlockYaw(data);
	}

	public static boolean checkLegalityStatic(CfaReadableMotionData data, WallInfo wall, Vec3d checkOffset) {
		Vec3d alteredPos = data.getPlayer().getPos().add(checkOffset);
		BlockPos blockPos = new BlockPos(
				MathHelper.floor(alteredPos.x),
				MathHelper.floor(alteredPos.y),
				MathHelper.floor(alteredPos.z)
		);
		return ClimbTransitions.verifyIntangibleDirectionalClimbingLegality(data.getPlayer(), blockPos, Direction.fromRotation(wall.getWallYaw()), true);
//		BlockState state = data.getPlayer().getWorld().getBlockState(blockPos);
//		return wall.calculateWallYaw() == getYawOf(data.getPlayer(), blockPos, state);
	}

	public static boolean useAlternateOffset(CfaReadableMotionData data) {
		return !ClimbTransitions.verifyIntangibleDirectionalClimbingLegality(data.getPlayer(), data.getPlayer().getBlockPos(),
				Direction.fromRotation(HelperGetter.getWallboundActionHelper().getWallInfo(data).getWallYaw()),
				false);
	}

	@Override
	protected boolean useAlternateOffset(CfaData data) {
		return useAlternateOffset((CfaReadableMotionData) data);
	}

	@Override public boolean verifyLegality(CfaReadableMotionData data, WallInfo wall, Vec3d checkOffset) {
		return checkLegalityStatic(data, wall, checkOffset);
	}

	@Override
	public void clientTick(CfaClientData data, boolean isSelf) {

	}

	@Override
	protected Identifier getSideHangActionID() {
		return ClimbIntangibleSideHang.ID;
	}

	public static final ActionTransitionDetails.ClientsExecutor SIDE_HANG_CLIENTS_EXECUTOR = (data, isSelf, seed) -> {
		ClimbTransitions.playGrabIntangibleSound(data, seed);
	};

	@Override
	protected ActionTransitionDetails.ClientsExecutor getSideHangTransitionClientsExecutor() {
		return SIDE_HANG_CLIENTS_EXECUTOR;
	}

	@Override
	protected double getConstantTowardsWallVel() {
		return 0;
	}
}
