package com.fqf.mario_qua_mario.actions.wallbound;

import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public class ClimbIntangibleSideHang extends ClimbWallSideHang implements WallboundActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("climb_intangible_side_hang");
	@Override public @NotNull Identifier defineID() {
	    return ID;
	}

	@Override
	protected float getEntireBodyXOffset(CfaReadableMotionData data) {
		return data.retrieveStateData(ClimbOmniDirectionalVars.class).ALTERNATE_OFFSET ? 2.75F : 1;
	}

	@Override
	public float getWallYaw(CfaReadableMotionData data) {
		return ClimbIntangibleDirectional.currentBlockYaw(data);
	}

	@Override
	public boolean checkLegality(CfaReadableMotionData data, WallInfo wall, Vec3d checkOffset) {
		return ClimbIntangibleDirectional.checkLegalityStatic(data, wall, checkOffset);
	}

	@Override
	public void clientTick(CfaClientData data, boolean isSelf) {

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
	protected boolean useAlternateOffset(CfaData data) {
		return ClimbIntangibleDirectional.useAlternateOffset((CfaReadableMotionData) data);
	}

	@Override
	protected TransitionDefinition.ClientsExecutor getSideHangTransitionClientsExecutor() {
		return ClimbIntangibleDirectional.SIDE_HANG_CLIENTS_EXECUTOR;
	}
}
