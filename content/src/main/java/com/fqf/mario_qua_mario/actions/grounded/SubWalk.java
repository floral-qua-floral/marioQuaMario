package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.charaformact_api.cfadata.*;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.Jump;
import com.fqf.mario_qua_mario.actions.aquatic.UnderwaterWalk;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class SubWalk implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("sub_walk");

	@Override public @Nullable CameraAnimationSet defineCameraAnimations(AnimationHelper helper) {
		return null;
	}
	@Override public @NotNull SlidingStatus defineSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.ALLOW;
	}
	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable BappingRule defineBappingRule() {
		return null;
	}
	@Override public @Nullable Identifier defineActiveCollisionAttack() {
		return null;
	}

	public static final CfaStat WALK_ACCEL = new CfaStat(0.045, WALKING, FORWARD, ACCELERATION);
	public static final CfaStat WALK_STANDSTILL_ACCEL = new CfaStat(0.125, WALKING, FORWARD, ACCELERATION, FRICTION);
	public static final CfaStat WALK_STANDSTILL_THRESHOLD = new CfaStat(0.12, WALKING, THRESHOLD);
	public static final CfaStat WALK_SPEED = new CfaStat(0.275, WALKING, SPEED, FORWARD);
	public static final CfaStat WALK_REDIRECTION = new CfaStat(0.0, WALKING, FORWARD, REDIRECTION);

	public static final CfaStat OVERWALK_ACCEL = new CfaStat(0.028, WALKING, FORWARD, OVERSPEED_CORRECTION);

	public static final CfaStat IDLE_DEACCEL = new CfaStat(0.075, WALKING, FRICTION);

	public static final CfaStat BACKPEDAL_ACCEL = new CfaStat(0.055, WALKING, BACKWARD, ACCELERATION);
	public static final CfaStat BACKPEDAL_SPEED = new CfaStat(0.225, WALKING, BACKWARD, SPEED);
	public static final CfaStat BACKPEDAL_REDIRECTION = new CfaStat(0.0, WALKING, BACKWARD, REDIRECTION);
	public static final CfaStat OVERBACKPEDAL_ACCEL = new CfaStat(0.04, WALKING, BACKWARD, OVERSPEED_CORRECTION);
	public static final CfaStat UNDERBACKPEDAL_ACCEL = new CfaStat(0.055, WALKING, BACKWARD, ACCELERATION, FRICTION);

	public static final CfaStat STRAFE_ACCEL = new CfaStat(0.065, WALKING, STRAFE, ACCELERATION);
	public static final CfaStat STRAFE_SPEED = new CfaStat(0.275, WALKING, STRAFE, SPEED);

	@Override public @Nullable Object provideStateData(CfaData data) {
		return null;
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {

	}
	@Override public void serverTick(CfaAuthoritativeData data) {

	}
	@Override public void travelHook(CfaTravelData data, GroundedActionHelper helper) {
		if(data.getInputs().getForwardInput() > 0) {
			if(data.getForwardVel() > WALK_SPEED.getAsLimit(data)) {
				// Overwalk
				helper.groundAccel(data,
						OVERWALK_ACCEL, WALK_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
						WALK_REDIRECTION
				);
			}
			else if(data.getForwardVel() <= WALK_STANDSTILL_THRESHOLD.getAsThreshold(data)) {
				// Walk accel from low velocity
				helper.groundAccel(data,
						WALK_STANDSTILL_ACCEL, WALK_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
						WALK_REDIRECTION
				);
			}
			else {
				// Walk accel
				helper.groundAccel(data,
						WALK_ACCEL, WALK_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
						WALK_REDIRECTION
				);
			}
		}
		else if(data.getInputs().getForwardInput() < 0) {
			if(data.getForwardVel() > 0) {
				// Under-backpedal
				helper.groundAccel(data,
						UNDERBACKPEDAL_ACCEL, BACKPEDAL_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
						BACKPEDAL_REDIRECTION
				);
			}
			else if(data.getForwardVel() < BACKPEDAL_SPEED.getAsLimit(data)) {
				// Over-backpedal
				helper.groundAccel(data,
						OVERBACKPEDAL_ACCEL, BACKPEDAL_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
						BACKPEDAL_REDIRECTION
				);
			}
			else {
				// Backpedal Accel
				helper.groundAccel(data,
						BACKPEDAL_ACCEL, BACKPEDAL_SPEED,
						STRAFE_ACCEL, STRAFE_SPEED,
						data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
						BACKPEDAL_REDIRECTION
				);
			}
		}
		else {
			// Idle deaccel
			helper.groundAccel(data,
					IDLE_DEACCEL, CfaStat.ZERO,
					STRAFE_ACCEL, STRAFE_SPEED,
					0.0, 0.0, CfaStat.ZERO
			);
		}
	}

	public static boolean isIdle(CfaReadableMotionData data) {
		return MathHelper.approximatelyEquals(data.getForwardVel(), 0)
				&& MathHelper.approximatelyEquals(data.getStrafeVel(), 0)
				&& MathHelper.approximatelyEquals(data.getInputs().getForwardInput(), 0)
				&& MathHelper.approximatelyEquals(data.getInputs().getStrafeInput(), 0)
				&& !data.getInputs().DUCK.isHeld();
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(
				DuckWaddle.DUCK,
				new ActionTransitionDetails(
						WalkRun.ID,
						WalkRun::meetsWalkRunRequirement,
						EvaluatorEnvironment.CLIENT_ONLY
				)//,
//				new ActionTransitionDetails(
//						RetroIdle.ID,
//						data -> data.hasPower(Powers.SMB3_IDLE) && isIdle(data),
//						EvaluatorEnvironment.CLIENT_ONLY
//				)
		);
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(
				Skid.SKID,
				Jump.makeJumpTransition(helper)
		);
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(
				Fall.FALL,
				UnderwaterWalk.SUBMERGE
		);
	}
}
