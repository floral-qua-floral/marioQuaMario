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

public class ClimbIntangibleSideHang extends ClimbIntangibleDirectional implements WallboundActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("climb_intangible_side_hang");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return ClimbWallSideHang.makeAnimation(helper, 1);
	}

	@Override
	public @NotNull WallBodyAlignment getBodyAlignment() {
		return WallBodyAlignment.SIDEWAYS;
	}

	@Override
	public float getHeadYawRange() {
		return 360;
	}

	@Override
	public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return null;
	}

	@Override
	public void clientTick(IMarioClientData data, boolean isSelf) {

	}

	@Override
	public void travelHook(IMarioTravelData data, WallInfo wall, WallboundActionHelper helper) {
		ClimbWallSideHang.sideHangTravelHook(data, wall, helper, this);
	}

	@Override
	public @NotNull List<TransitionDefinition> getBasicTransitions(WallboundActionHelper helper) {
		return List.of(
				ClimbWallSideHang.RETURN_TO_NORMAL_CLIMB.variate(
						ClimbIntangibleDirectional.ID,
						null,
						null,
						null,
						this.makeSideHangTransitionClientsExecutor()
				)
		);
	}

	@Override
	public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of(
				ClimbWallSideHang.makeTransitionInjection(ClimbIntangibleDirectional.ID, this, ID)
		);
	}
}
