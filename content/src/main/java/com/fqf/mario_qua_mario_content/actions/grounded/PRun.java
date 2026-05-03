package com.fqf.mario_qua_mario_content.actions.grounded;

import com.fqf.charapoweract_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.*;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.*;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charapoweract_api.cpadata.*;
import com.fqf.charapoweract_api.util.CharaStat;
import com.fqf.charapoweract_api.util.Easing;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.airborne.Fall;
import com.fqf.mario_qua_mario_content.actions.airborne.Jump;
import com.fqf.mario_qua_mario_content.actions.airborne.PJump;
import com.fqf.mario_qua_mario_content.actions.aquatic.UnderwaterWalk;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.charapoweract_api.util.StatCategory.*;

public class PRun implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("p_run");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null,
				new ProgressHandler((data, ticksPassed) ->
						Easing.clampedRangeToProgress(data.getDeltaYaw(), -1, 1) * 2 - 1),
				new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) ->
						arrangement.roll = progress * -5),
				null, null,
				new LimbAnimation(false, (data, arrangement, progress) ->
						arrangement.roll += 90 - progress * 10),
				new LimbAnimation(false, (data, arrangement, progress) ->
						arrangement.roll += -90 - progress * 10),
				null, null,
				new LimbAnimation(false, null)
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

	public static final CharaStat P_SPEED = new CharaStat(0.5875, P_RUNNING, FORWARD, SPEED);
	public static final CharaStat P_ACCEL = new CharaStat(0.13, P_RUNNING, FORWARD, ACCELERATION);
	public static final CharaStat P_REDIRECTION = new CharaStat(6.0, P_RUNNING, FORWARD, REDIRECTION);

	@Override public @Nullable BappingRule getBappingRule() {
		return null;
	}
	@Override public @Nullable Identifier getCollisionAttackTypeID() {
		return null;
	}

	@Override public @Nullable Object provideStateData(ICPAData data) {
		return null;
	}
	@Override public void clientTick(ICPAClientData data, boolean isSelf) {

	}
	@Override public void serverTick(ICPAAuthoritativeData data) {

	}
	@Override public void travelHook(ICPATravelData data, GroundedActionHelper helper) {
		boolean sprinting = data.getPlayer().isSprinting();
		helper.groundAccel(data,
				sprinting ? WalkRun.OVERRUN_ACCEL : WalkRun.OVERWALK_ACCEL,
				sprinting ? P_SPEED : SubWalk.WALK_SPEED,
				CharaStat.ZERO, CharaStat.ZERO,
				1, data.getInputs().getStrafeInput() * 0.5,
				P_REDIRECTION
		);
	}

	public static boolean meetsPRunRequirements(ICPAReadableMotionData data) {
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
