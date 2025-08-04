package com.fqf.mario_qua_mario_content.actions.wallbound;

import com.fqf.mario_qua_mario_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ClimbIntangibleSideHang extends ClimbWallSideHang implements WallboundActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("climb_intangible_side_hang");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	protected float getEntireBodyXOffset(IMarioReadableMotionData data) {
		return data.getVars(ClimbOmniDirectionalVars.class).ALTERNATE_OFFSET ? 2.75F : 1;
	}

	@Override
	public float getWallYaw(IMarioReadableMotionData data) {
		return ClimbIntangibleDirectional.currentBlockYaw(data);
	}

	@Override
	public boolean checkLegality(IMarioReadableMotionData data, WallInfo wall, Vec3d checkOffset) {
		return ClimbIntangibleDirectional.checkLegalityStatic(data, wall, checkOffset);
	}

	@Override
	public void clientTick(IMarioClientData data, boolean isSelf) {

	}

	@Override
	protected double getConstantTowardsWallVel() {
		return 0;
	}

	@Override
	protected Identifier getClimbingActionID() {
		return ClimbIntangibleDirectional.ID;
	}

	@Override
	protected boolean useAlternateOffset(IMarioData data) {
		return ClimbIntangibleDirectional.useAlternateOffset((IMarioReadableMotionData) data);
	}

	@Override
	protected TransitionDefinition.ClientsExecutor getSideHangTransitionClientsExecutor() {
		return ClimbIntangibleDirectional.SIDE_HANG_CLIENTS_EXECUTOR;
	}
}
