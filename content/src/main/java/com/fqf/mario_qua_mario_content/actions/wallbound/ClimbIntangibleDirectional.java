package com.fqf.mario_qua_mario_content.actions.wallbound;

import com.fqf.mario_qua_mario_api.mariodata.*;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_content.util.ClimbTransitions;
import net.minecraft.block.BlockState;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class ClimbIntangibleDirectional extends ClimbWall implements WallboundActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("climb_intangible_directional");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	private static float getYawOf(BlockState state) {
		return ClimbTransitions.yawOf(ClimbTransitions.hasDirectionality(state));
	}
	protected static float currentBlockYaw(IMarioReadableMotionData data) {
		return getYawOf(data.getMario().getBlockStateAtPos());
	}

	@Override
	public float getWallYaw(IMarioReadableMotionData data) {
		return currentBlockYaw(data);
	}

	@Override public boolean checkLegality(IMarioReadableMotionData data, WallInfo wall, Vec3d checkOffset) {
		Vec3d alteredPos = data.getMario().getPos().add(checkOffset);
		BlockState state = data.getMario().getWorld().getBlockState(new BlockPos(
				MathHelper.floor(alteredPos.x),
				MathHelper.floor(alteredPos.y),
				MathHelper.floor(alteredPos.z)
		));
		return ClimbTransitions.isNonSolidClimbable(data, state, true) && wall.getWallYaw() == getYawOf(state);
	}

	@Override
	public void clientTick(IMarioClientData data, boolean isSelf) {

	}

	@Override
	protected Identifier getSideHangActionID() {
		return ClimbIntangibleSideHang.ID;
	}

	@Override
	protected TransitionDefinition.ClientsExecutor makeSideHangTransitionClientsExecutor() {
		return (data, isSelf, seed) -> {
			BlockSoundGroup group = data.getMario().getBlockStateAtPos().getSoundGroup();
			data.playSound(group.getStepSound(), group.getPitch(), 1, seed);
		};
	}

	@Override
	protected double getConstantTowardsWallVel() {
		return 0;
	}
}
