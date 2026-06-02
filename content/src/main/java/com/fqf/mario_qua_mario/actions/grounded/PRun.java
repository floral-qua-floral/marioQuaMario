package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.charaformact_api.cfadata.*;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.Easing;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.Jump;
import com.fqf.mario_qua_mario.actions.airborne.PJump;
import com.fqf.mario_qua_mario.actions.aquatic.UnderwaterWalk;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class PRun implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("p_run");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	private static float getTilt(CfaAnimatingData data) {
		return Easing.clampedRangeToProgress(data.getDeltaYaw(), -1, 1) * 2 - 1;
	}

	@Override public @Nullable AnimationDefinition getAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_ARMS,
				(arrangement, data, animationTime, helper) -> arrangement.roll = getTilt(data) * -5,
				(posture, data, animationTime, helper) -> {
					float tilt = getTilt(data);
					posture.RIGHT_ARM.roll += 90 - tilt * 10;
					posture.LEFT_ARM.roll -= 90 + tilt * 10;

					if(posture.TAIL != null)
						posture.TAIL.setAngles(0, 0, 0);
				}
		);
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.NOT_SLIDING_SMOOTH;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.ALLOW;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.ALLOW;
	}

	public static final CfaStat P_SPEED = new CfaStat(0.5875, P_RUNNING, FORWARD, SPEED);
	public static final CfaStat P_ACCEL = new CfaStat(0.13, P_RUNNING, FORWARD, ACCELERATION);
	public static final CfaStat P_REDIRECTION = new CfaStat(6.0, P_RUNNING, FORWARD, REDIRECTION);

	@Override public @Nullable BappingRule getBappingRule() {
		return null;
	}
	@Override public @Nullable Identifier getCollisionAttackTypeID() {
		return null;
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return null;
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {

	}
	@Override public void serverTick(CfaAuthoritativeData data) {

	}
	@Override public void travelHook(CfaTravelData data, GroundedActionHelper helper) {
		boolean sprinting = data.getPlayer().isSprinting();
		helper.groundAccel(data,
				sprinting ? WalkRun.OVERRUN_ACCEL : WalkRun.OVERWALK_ACCEL,
				sprinting ? P_SPEED : SubWalk.WALK_SPEED,
				CfaStat.ZERO, CfaStat.ZERO,
				1, data.getInputs().getStrafeInput() * 0.5,
				P_REDIRECTION
		);
	}

	public static boolean meetsPRunRequirements(CfaReadableMotionData data) {
		return data.getInputs().getForwardInput() > 0 &&
				data.getForwardVel() > SubWalk.WALK_SPEED.get(data) &&
				data.getHorizVelSquared() > WalkRun.RUN_SPEED.getAsSquaredThreshold(data);
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper) {
		return List.of(
				DuckWaddle.DUCK,
				Skid.SKID,
				new TransitionDefinition(
						SubWalk.ID,
						data -> {
							double threshold = WalkRun.RUN_SPEED.getAsThreshold(data);
							return
									data.getForwardVel() <= 0
									|| data.getHorizVelSquared() < threshold * threshold
									|| Math.abs(data.getStrafeVel()) > 0.175;
						},
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(GroundedActionHelper helper) {
		return List.of(
				Jump.makeJumpTransition(helper).variate(PJump.ID, null)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(GroundedActionHelper helper) {
		return List.of(
				Fall.FALL,
				UnderwaterWalk.SUBMERGE
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override
	public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
