package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.airborne.BonkAir;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.Jump;
import com.fqf.mario_qua_mario.actions.airborne.Sideflip;
import com.fqf.mario_qua_mario.actions.aquatic.UnderwaterWalk;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class Skid implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("skid");

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(arrangement, data, animationTime, helper) -> {
					arrangement.y -= 4;
					arrangement.setAngles(
							0,
							42.5F,
							17.5F
					);
				},
				(posture, data, animationTime, helper) -> {
					posture.HEAD.roll -= 15;

					posture.RIGHT_ARM.addAngles(-32, -35, 80);
					posture.LEFT_ARM.addAngles(-45, 0, -30);
					posture.RIGHT_LEG.addAngles(-57.5F, 45F, -20);
					posture.LEFT_LEG.addPos(0, -4.1F, -3.9F);
					posture.LEFT_LEG.addAngles(5, 15, 0);

					if(posture.TAIL != null)
						posture.TAIL.setAngles(5, 42, -17.5F);
				}
		);
	}

	@Override public @NotNull SlidingStatus defineSlidingStatus() {
		return SlidingStatus.SKIDDING;
	}

	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable BappingRule defineBappingRule() {
		return new BappingRule(0, 0, 3, new CfaStat(0, THRESHOLD));
	}

	public static final CfaStat SKID_THRESHOLD = new CfaStat(0.285, RUNNING, THRESHOLD);

	public static final CfaStat SKID_DRAG = new CfaStat(0.185, RUNNING, DRAG);
	public static final CfaStat SKID_DRAG_MIN = new CfaStat(0.045, RUNNING, DRAG);
	public static final CfaStat SKID_REDIRECTION = new CfaStat(4.5, RUNNING, REDIRECTION);

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new ActionTimerVars();
	}
	@Override public void travelHook(CfaTravelData data, GroundedActionHelper helper) {
		data.retrieveStateData(ActionTimerVars.class).actionTimer++;
		helper.applyDrag(
				data, SKID_DRAG, SKID_DRAG_MIN,
				-data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				SKID_REDIRECTION
		);
	}

	public static final ActionTransitionDetails SKID = new ActionTransitionDetails(
			ID,
			data -> data.getInputs().getForwardInput() < -0.675 && data.getForwardVel() > SKID_THRESHOLD.get(data),
			EvaluatorEnvironment.CLIENT_ONLY
	);

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(new ActionTransitionDetails(
				SubWalk.ID,
				data -> data.getHorizVelSquared() == 0 || data.getInputs().getForwardInput() >= 0,
				EvaluatorEnvironment.CLIENT_ONLY
		));
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(
				new ActionTransitionDetails(
						Sideflip.ID,
						data -> data.getForwardVel() < Sideflip.SIDEFLIP_THRESHOLD.get(data) && data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> {
							helper.performJump(data, Sideflip.SIDEFLIP_VEL, null);
							data.setForwardStrafeVel(Sideflip.SIDEFLIP_BACKWARDS_SPEED.get(data), 0);
							data.forceBodyAlignment(true);
						},
						(data, isSelf, seed) -> {
							data.forceBodyAlignment(true);
							data.playJumpSound(seed);
							data.voice(Voicelines.SIDEFLIP, seed);
						}
				),
				Jump.makeJumpTransition(helper)
		);
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(
				Fall.FALL,
				UnderwaterWalk.SUBMERGE,
				BonkAir.BONK.variate(
						BonkGround.BACKWARD_ID,
						null,
						null,
						data ->
								data.setVelocity(data.getRecordedCollisions().getHorizontallyReflectedVelocity().multiply(1.25)),
						null
				)
		);
	}
}
