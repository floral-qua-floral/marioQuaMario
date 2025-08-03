package com.fqf.mario_qua_mario_content.actions.wallbound;

import com.fqf.mario_qua_mario_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.WallBodyAlignment;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ClimbIntangibleSideHang extends ClimbIntangibleDirectional implements WallboundActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("climb_intangible_side_hang");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	private static LimbAnimation makeArmAnimation(int factor) {
	    return new LimbAnimation(false, (data, arrangement, progress) -> {

	    });
	}
	private static LimbAnimation makeLegAnimation(int factor) {
	    return new LimbAnimation(false, (data, arrangement, progress) -> {

	    });
	}
	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
	    return new PlayermodelAnimation(
	            null,
	            new ProgressHandler((data, ticksPassed) -> 1),
	            new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {

	            }),
	            null,
	            null,
	            makeArmAnimation(1), makeArmAnimation(-1),
	            makeLegAnimation(1), makeLegAnimation(-1),
	            null
	    );
	}

	@Override
	public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return null;
	}

	@Override
	public boolean checkServerSidedLegality(IMarioReadableMotionData data, WallInfo wall) {
		return super.checkServerSidedLegality(data, wall);
	}

	@Override
	public @NotNull WallBodyAlignment getBodyAlignment() {
		return WallBodyAlignment.SIDEWAYS;
	}

	@Override
	public void travelHook(IMarioTravelData data, WallInfo wall, WallboundActionHelper helper) {

	}

	@Override
	public @NotNull List<TransitionDefinition> getBasicTransitions(WallboundActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						ClimbIntangibleDirectional.ID,
						data -> Math.abs(helper.getWallInfo(data).getYawDeviation()) < 80,
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> data.setVelocity(Vec3d.ZERO),
						null
				)
		);
	}

	@Override
	public @NotNull List<TransitionDefinition> getInputTransitions(WallboundActionHelper helper) {
		return super.getInputTransitions(helper);
	}

	@Override
	public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(WallboundActionHelper helper) {
		return super.getWorldCollisionTransitions(helper);
	}
}
