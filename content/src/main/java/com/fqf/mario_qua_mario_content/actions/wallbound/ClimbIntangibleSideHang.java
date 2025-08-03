package com.fqf.mario_qua_mario_content.actions.wallbound;

import com.fqf.mario_qua_mario_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.util.ClimbTransitions;
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

	private static LimbAnimation makeArmAnimation(int factor) {
	    return new LimbAnimation(false, (data, arrangement, progress) -> {
			boolean isWallSide = progress == factor;

			arrangement.roll += factor * (isWallSide ? 140 : 12.5F);
			arrangement.pitch += isWallSide ? -25 : 0;
	    });
	}
	private static LimbAnimation makeLegAnimation(int factor) {
	    return new LimbAnimation(false, (data, arrangement, progress) -> {
			boolean isWallSide = progress == factor;
			if(isWallSide) {
				arrangement.addPos(factor * -0.5F, -3.75F, -4);
				arrangement.addAngles(4, 0, factor * -2);
			}
			else {
				arrangement.pitch += 17.5F;
				arrangement.roll += factor * -30;
			}
	    });
	}
	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
	    return new PlayermodelAnimation(
	            null,
	            new ProgressHandler((data, ticksPassed) -> Math.signum(Objects.requireNonNull(helper.getWallInfo(data)).getYawDeviation())),
	            new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {
					arrangement.x += progress * 1;
					arrangement.roll = progress * 12.5F;
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
		if(data.getYVel() < 0) {
			data.setYVel(data.getYVel() * 0.775);
		}
		else data.setYVel(0);
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
	public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of(
				new TransitionInjectionDefinition(
						TransitionInjectionDefinition.InjectionPlacement.BEFORE,
						ClimbIntangibleDirectional.ID,
						(fromAction, fromCategory, existingTransitions) -> fromCategory != ActionCategory.WALLBOUND,
						(nearbyTransition, castableHelper) -> nearbyTransition.variate(
								ClimbIntangibleSideHang.ID,
								data -> MathHelper.angleBetween(data.getMario().getYaw(), currentBlockYaw(data)) > 90
										&& nearbyTransition.evaluator().shouldTransition(data)
						)
				)
		);
	}
}
