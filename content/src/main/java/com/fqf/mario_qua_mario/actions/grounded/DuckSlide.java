package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.TransitionInjectionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.airborne.Backflip;
import com.fqf.mario_qua_mario.actions.airborne.DuckFall;
import com.fqf.mario_qua_mario.actions.airborne.DuckJump;
import com.fqf.mario_qua_mario.actions.airborne.LongJump;
import com.fqf.mario_qua_mario.actions.aquatic.UnderwaterDuck;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class DuckSlide implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("duck_slide");
	@Override public @NotNull Identifier defineID() {
	    return ID;
	}

	@Override
	public @Nullable AnimationDefinition defineAnimation() {
		return DuckWaddle.makeAnimation(true, false);
	}

	@Override public @NotNull SlidingStatus defineSlidingStatus() {
		return SlidingStatus.SLIDING;
	}

	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.SLIP;
	}
	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	public static final CfaStat SLIDE_THRESHOLD = new CfaStat(0.25, DUCKING, THRESHOLD);
	public static final CfaStat SLIDE_BOOST = new CfaStat(-0.15);

	public static final CfaStat SLIDE_DRAG = new CfaStat(0.04333, DUCKING, DRAG);
	public static final CfaStat SLIDE_DRAG_MIN = new CfaStat(0.01, DUCKING, DRAG);
	public static final CfaStat SLIDE_REDIRECTION = new CfaStat(4.0, DUCKING, REDIRECTION);

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new ActionTimerVars();
	}
	@Override public void travelHook(CfaTravelData data, GroundedActionHelper helper) {
		data.retrieveStateData(ActionTimerVars.class).actionTimer++;
		helper.applyDrag(
				data, SLIDE_DRAG, SLIDE_DRAG_MIN,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				SLIDE_REDIRECTION
		);
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(
				DuckWaddle.UNDUCK,
				new ActionTransitionDetails(
						DuckWaddle.ID,
						data -> data.getHorizVelSquared() == 0,
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(
				Backflip.makeBackflipTransition(helper),
				new ActionTransitionDetails(
						LongJump.ID,
						data ->
								data.getInputs().getForwardInput() > 0.4
										&& data.retrieveStateData(ActionTimerVars.class).actionTimer < 5
										&& data.getForwardVel() > LongJump.LONG_JUMP_THRESHOLD.get(data)
										&& data.getInputs().JUMP.isPressed(),
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> {
							helper.performJump(data, LongJump.LONG_JUMP_VEL, null);
							data.setForwardVel(data.getForwardVel() * 0.92 + 0.098);
							data.getInputs().DUCK.isPressed(); // Force unbuffer DUCK
						},
						(data, isSelf, seed) -> {
							data.playJumpSound(seed);
							data.voice(Voicelines.LONG_JUMP, seed);
						}
				),
				DuckJump.makeDuckJumpTransition(helper)
		);
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, GroundedActionHelper helper) {
		builder.add(
				DuckFall.DUCK_FALL,
				UnderwaterDuck.DUCK_SUBMERGE
		);
	}

	public static final TransitionInjectionDefinition GROUNDED_INJECTION = new TransitionInjectionDefinition.Simple(
			TransitionInjectionDefinition.InjectionPlacement.BEFORE, DuckWaddle.ID,
			ActionCategory.GROUNDED,
			(nearbyTransition, helper) -> nearbyTransition.variate(
					DuckSlide.ID,
					data -> {
						double threshold = SLIDE_THRESHOLD.get(data);
						return data.getHorizVelSquared() > threshold * threshold
								&& nearbyTransition.evaluator().shouldTransition(data);
					},
					EvaluatorEnvironment.CLIENT_ONLY,
					null, null
			)
	);
	public static final TransitionInjectionDefinition AIRBORNE_INJECTION = new TransitionInjectionDefinition.Simple(
			TransitionInjectionDefinition.InjectionPlacement.BEFORE, DuckWaddle.ID,
			ActionCategory.AIRBORNE,
			(nearbyTransition, helper) -> nearbyTransition.variate(
					DuckSlide.ID,
					data -> {
						double threshold = SLIDE_THRESHOLD.get(data);
						return (data.isServer() || (data.getHorizVelSquared() > threshold * threshold))
								&& nearbyTransition.evaluator().shouldTransition(data);
					},
					EvaluatorEnvironment.CLIENT_CHECKED,
					null, null
			)
	);

}
