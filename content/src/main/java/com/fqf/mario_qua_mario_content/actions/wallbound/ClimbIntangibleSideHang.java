package com.fqf.mario_qua_mario_content.actions.wallbound;

import com.fqf.charapoweract_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.*;
import com.fqf.charapoweract_api.cpadata.ICPAClientData;
import com.fqf.charapoweract_api.cpadata.ICPAData;
import com.fqf.charapoweract_api.cpadata.ICPAReadableMotionData;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class ClimbIntangibleSideHang extends ClimbWallSideHang implements WallboundActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("climb_intangible_side_hang");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	protected float getEntireBodyXOffset(ICPAReadableMotionData data) {
		return data.retrieveStateData(ClimbOmniDirectionalVars.class).ALTERNATE_OFFSET ? 2.75F : 1;
	}

	@Override
	public float getWallYaw(ICPAReadableMotionData data) {
		return ClimbIntangibleDirectional.currentBlockYaw(data);
	}

	@Override
	public boolean checkLegality(ICPAReadableMotionData data, WallInfo wall, Vec3d checkOffset) {
		return ClimbIntangibleDirectional.checkLegalityStatic(data, wall, checkOffset);
	}

	@Override
	public void clientTick(ICPAClientData data, boolean isSelf) {

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
	protected boolean useAlternateOffset(ICPAData data) {
		return ClimbIntangibleDirectional.useAlternateOffset((ICPAReadableMotionData) data);
	}

	@Override
	protected TransitionDefinition.ClientsExecutor getSideHangTransitionClientsExecutor() {
		return ClimbIntangibleDirectional.SIDE_HANG_CLIENTS_EXECUTOR;
	}
}
