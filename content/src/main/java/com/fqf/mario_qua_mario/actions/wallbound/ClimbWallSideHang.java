package com.fqf.mario_qua_mario.actions.wallbound;

import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.WallboundActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.*;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.EntireBodyAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.LimbAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.PiecemealPlayermodelAnimation;
import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ClimbWallSideHang extends ClimbWall implements WallboundActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("climb_wall_side_hang");
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
	protected float getEntireBodyXOffset(CfaReadableMotionData data) {
		return -1.25F;
	}
	@Override public @Nullable PiecemealPlayermodelAnimation getOldAnimation(AnimationHelper helper) {
		return new PiecemealPlayermodelAnimation(
				null,
				new ProgressHandler((data, ticksPassed) -> Math.signum(Objects.requireNonNull(helper.getWallInfo(data)).getYawDeviation())),
				new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {
					arrangement.x += progress * this.getEntireBodyXOffset(data);
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
	public @NotNull WallBodyAlignment getBodyAlignment() {
		return WallBodyAlignment.SIDEWAYS;
	}

	public static void sideHangTravelHook(CfaTravelData data, WallInfo wall, WallboundActionHelper helper, ClimbWall baseAction) {
		helper.setTowardsWallVel(data, baseAction.TOWARDS_WALL_VEL);
		if(data.getYVel() < -0.05) {
			data.setYVel(data.getYVel() * 0.775);
		}
		else {
			data.getPlayer().fallDistance = 0;
			data.setYVel(0);
		}
	}

	@Override
	public void travelHook(CfaTravelData data, WallInfo wall, WallboundActionHelper helper) {
		sideHangTravelHook(data, wall, helper, this);
	}

	protected Identifier getClimbingActionID() {
		return ClimbWall.ID;
	}

	@Override
	public @NotNull List<TransitionDefinition> getBasicTransitions(WallboundActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						this.getClimbingActionID(),
						data -> Math.abs(helper.getWallInfo(data).getYawDeviation()) < 80,
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> data.setVelocity(Vec3d.ZERO),
						this.getSideHangTransitionClientsExecutor()
				)
		);
	}

	@Override
	public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of(
				new TransitionInjectionDefinition(
						TransitionInjectionDefinition.InjectionPlacement.BEFORE,
						this.getClimbingActionID(),
						(fromAction, fromCategory, existingTransitions) -> fromCategory != ActionCategory.WALLBOUND,
						(nearbyTransition, castableHelper) -> nearbyTransition.variate(
								this.getID(),
								data -> (data.isServer() || MathHelper.angleBetween(data.getPlayer().getYaw(), this.getWallYaw(data)) > ClimbWall.MIN_DEVIATION_TO_SIDE_HANG)
										&& nearbyTransition.evaluator().shouldTransition(data)
						)
				)
		);
	}
}
