package com.fqf.mario_qua_mario.actions.aquatic;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.LavaBoost;
import com.fqf.mario_qua_mario.actions.airborne.SpecialFall;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class Submerged implements AquaticActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("submerged");

	protected float getAnimationProgress(float animationTime) {
		return 3;
	}

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return AnimationDefinition.of(
				EnumSet.of(AnimationFlag.NO_RIGHT_ARM_SWING, AnimationFlag.NO_LEFT_ARM_SWING, AnimationFlag.CAN_RESET_ON_SELF),
				(posture, data, animationTime, helper) -> {
					posture.TORSO.pitch += 15;

					float progress = this.getAnimationProgress(animationTime);
					helper.symmetricallyAnimate(posture, posture.RIGHT_ARM, arrangement -> {
						arrangement.roll *= -1;
						arrangement.addAngles(
								helper.interpolateKeyframes(progress,
										-75,
										-75,
										-75,
										-50
								),
								helper.interpolateKeyframes(progress,
										5,
										5,
										-165F,
										-40
								),
								helper.interpolateKeyframes(progress,
										125,
										125,
										100,
										60
								)
						);
					});

					helper.symmetricallyAnimate(posture, posture.RIGHT_LEG, arrangement -> {
						arrangement.pitch *= 0.5F;
						arrangement.addPos(
								-0.675F,
								-1.2F,
								-1.8F
						);
						arrangement.addAngles(
								50,
								6,
								0
						);
					});
				}
		);
	}

	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable BappingRule defineBappingRule() {
		return new BappingRule(3, 0);
	}

	public static final CfaStat FALL_ACCEL = new CfaStat(-0.035, AQUATIC_GRAVITY);
	public static final CfaStat FALL_SPEED = new CfaStat(-0.675, AQUATIC_TERMINAL_VELOCITY);

	public static final CfaStat DRAG = new CfaStat(0.11, WATER_DRAG);
	public static final CfaStat DRAG_MIN = new CfaStat(0.01, WATER_DRAG);

	public static final CfaStat FORWARD_SWIM_ACCEL = new CfaStat(0.025, SWIMMING, FORWARD, ACCELERATION);
	public static final CfaStat FORWARD_SWIM_SPEED = new CfaStat(0.25, SWIMMING, FORWARD, SPEED);

	public static final CfaStat BACKWARD_SWIM_ACCEL = new CfaStat(0.035, SWIMMING, BACKWARD, ACCELERATION);
	public static final CfaStat BACKWARD_SWIM_SPEED = new CfaStat(0.2, SWIMMING, BACKWARD, SPEED);

	public static final CfaStat STRAFE_SWIM_ACCEL = new CfaStat(0.025, SWIMMING, STRAFE, ACCELERATION);
	public static final CfaStat STRAFE_SWIM_SPEED = new CfaStat(0.25, SWIMMING, STRAFE, SPEED);

	public static final CfaStat SWIM_REDIRECTION = new CfaStat(2.0, SWIMMING, REDIRECTION);

	public static void waterMove(CfaTravelData data, AquaticActionHelper helper) {
		helper.applyGravity(data, FALL_ACCEL, FALL_SPEED);
		helper.applyWaterDrag(data, DRAG, DRAG_MIN);
	}

	public static void drift(CfaTravelData data, AquaticActionHelper helper) {
		helper.aquaticAccel(data,
				FORWARD_SWIM_ACCEL, FORWARD_SWIM_SPEED,
				BACKWARD_SWIM_ACCEL, BACKWARD_SWIM_SPEED,
				STRAFE_SWIM_ACCEL, STRAFE_SWIM_SPEED,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				SWIM_REDIRECTION
		);
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new ActionTimerVars();
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {
		data.retrieveStateData(ActionTimerVars.class).actionTimer++;
	}
	@Override public void serverTick(CfaAuthoritativeData data) {
		data.retrieveStateData(ActionTimerVars.class).actionTimer++;
	}
	@Override public void travelHook(CfaTravelData data, AquaticActionHelper helper) {
		waterMove(data, helper);
		drift(data, helper);
	}

	public static final ActionTransitionDetails SUBMERGE = new ActionTransitionDetails(
			ID,
			data -> data.getImmersionPercent() > 0.5 && (data.getActionID() != LavaBoost.ID || data.getYVel() < 0),
			EvaluatorEnvironment.COMMON,
			data -> data.setYVel(data.getYVel() * 0.225),
			null
	);

	public static final ActionTransitionDetails EXIT_WATER = new ActionTransitionDetails(
			SpecialFall.ID,
			data -> data.getImmersionPercent() <= 0.3,
			EvaluatorEnvironment.COMMON
	);

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AquaticActionHelper helper) {
		builder.add(
				Swim.SWIM,
				AquaticPoundFlip.AQUATIC_GROUND_POUND,
				new ActionTransitionDetails(
						Paddle.ID,
						data -> {
							ActionTimerVars vars = data.retrieveStateData(ActionTimerVars.class);
							return (vars == null || vars.actionTimer > 7) && data.getInputs().JUMP.isHeld() && data.getForwardVel() > -0.1;
						},
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> data.setYVel(Math.max(Paddle.PADDLE_FALL_SPEED.get(data), data.getYVel())),
						null
				)
		);
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AquaticActionHelper helper) {
		builder.add(
				EXIT_WATER,
				Fall.LANDING.variate(UnderwaterWalk.ID, null)
		);
	}
}