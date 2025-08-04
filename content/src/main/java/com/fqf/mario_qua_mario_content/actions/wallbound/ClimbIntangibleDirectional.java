package com.fqf.mario_qua_mario_content.actions.wallbound;

import com.fqf.mario_qua_mario_api.HelperGetter;
import com.fqf.mario_qua_mario_api.mariodata.*;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_content.util.ClimbTransitions;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class ClimbIntangibleDirectional extends ClimbWall implements WallboundActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("climb_intangible_directional");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	@Override
	protected float getEntireBodyZOffset(IMarioReadableMotionData data) {
		return data.getVars(ClimbOmniDirectionalVars.class).ALTERNATE_OFFSET ? -1.75F : 0;
	}

	private static float getYawOf(PlayerEntity mario, BlockPos pos, BlockState state) {
		return ClimbTransitions.yawOf(ClimbTransitions.getIntangibleClimbableDirectionality(mario, pos, state));
	}
	protected static float currentBlockYaw(IMarioReadableMotionData data) {
		return getYawOf(data.getMario(), data.getMario().getBlockPos(), data.getMario().getBlockStateAtPos());
	}

	@Override
	public float getWallYaw(IMarioReadableMotionData data) {
		return currentBlockYaw(data);
	}

	public static boolean checkLegalityStatic(IMarioReadableMotionData data, WallInfo wall, Vec3d checkOffset) {
		Vec3d alteredPos = data.getMario().getPos().add(checkOffset);
		BlockPos blockPos = new BlockPos(
				MathHelper.floor(alteredPos.x),
				MathHelper.floor(alteredPos.y),
				MathHelper.floor(alteredPos.z)
		);
		return ClimbTransitions.verifyIntangibleDirectionalClimbingLegality(data.getMario(), blockPos, Direction.fromRotation(wall.getWallYaw()), true);
//		BlockState state = data.getMario().getWorld().getBlockState(blockPos);
//		return wall.getWallYaw() == getYawOf(data.getMario(), blockPos, state);
	}

	public static boolean useAlternateOffset(IMarioReadableMotionData data) {
		return !ClimbTransitions.verifyIntangibleDirectionalClimbingLegality(data.getMario(), data.getMario().getBlockPos(),
				Direction.fromRotation(HelperGetter.getWallboundActionHelper().getWallInfo(data).getWallYaw()),
				false);
	}

	@Override
	protected boolean useAlternateOffset(IMarioData data) {
		return useAlternateOffset((IMarioReadableMotionData) data);
	}

	@Override public boolean checkLegality(IMarioReadableMotionData data, WallInfo wall, Vec3d checkOffset) {
		return checkLegalityStatic(data, wall, checkOffset);
	}

	@Override
	public void clientTick(IMarioClientData data, boolean isSelf) {

	}

	@Override
	protected Identifier getSideHangActionID() {
		return ClimbIntangibleSideHang.ID;
	}

	public static final TransitionDefinition.ClientsExecutor SIDE_HANG_CLIENTS_EXECUTOR = (data, isSelf, seed) -> {
		ClimbTransitions.playGrabIntangibleSound(data, seed);
	};

	@Override
	protected TransitionDefinition.ClientsExecutor getSideHangTransitionClientsExecutor() {
		return SIDE_HANG_CLIENTS_EXECUTOR;
	}

	@Override
	protected double getConstantTowardsWallVel() {
		return 0;
	}


}
