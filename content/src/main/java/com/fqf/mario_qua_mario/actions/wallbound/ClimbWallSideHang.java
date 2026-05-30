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

	protected float getEntireBodyXOffset(CfaReadableMotionData data) {
		return -1.25F;
	}

	@Override public @Nullable AnimationDefinition getAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(arrangement, data, animationTime, helper) -> {
					float progress = Math.signum(Objects.requireNonNull(helper.getWallInfo(data)).getYawDeviation());
					arrangement.x += progress * this.getEntireBodyXOffset(data);
					arrangement.roll = progress * 12.5F;
				},
				(posture, data, animationTime, helper) -> {
					float progress = Math.signum(Objects.requireNonNull(helper.getWallInfo(data)).getYawDeviation());

					helper.asymmetricallyAnimate(posture.RIGHT_ARM, posture.LEFT_ARM, (arrangement, isLeft, sideFactor) -> {
						boolean isWallSide = progress == sideFactor;
						arrangement.addAngles(isWallSide ? -25 : 0, 0, sideFactor * (isWallSide ? 140 : 12.5F));
					});

					helper.asymmetricallyAnimate(posture.RIGHT_LEG, posture.LEFT_LEG, (arrangement, isLeft, sideFactor) -> {
						boolean isWallSide = progress == sideFactor;
						if(isWallSide) {
							arrangement.addPos(sideFactor * -0.5F, -3.75F, -4);
							arrangement.addAngles(4, 0, sideFactor * -2);
						}
						else {
							arrangement.addAngles(
									17.5F,
									0,
									sideFactor * -30
							);
						}
					});
				}
		);
	}

	@Override
	public @NotNull WallBodyAlignment getBodyAlignment() {
		return WallBodyAlignment.SIDEWAYS;
	}

	@Override
	public void travelHook(CfaTravelData data, WallInfo wall, WallboundActionHelper helper) {
		helper.setTowardsWallVel(data, TOWARDS_WALL_VEL);
		if(data.getYVel() < -0.05) {
			data.setYVel(data.getYVel() * 0.775);
		}
		else {
			data.getPlayer().fallDistance = 0;
			data.setYVel(0);
		}
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
