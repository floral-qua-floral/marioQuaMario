package com.fqf.mario_qua_mario.actions.form;

import com.fqf.charaformact_api.definitions.TransitionInjectionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.SneakingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.SprintingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.mario_qua_mario.actions.airborne.DuckFall;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.Jump;
import com.fqf.mario_qua_mario.actions.grounded.DuckWaddle;
import com.fqf.mario_qua_mario.util.Powers;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TailStallDucking extends TailStall implements AirborneActionDefinition {
	public static final Identifier ID = DUCK_STALL_ID; // prevents warnings from trying to access subclass field in TailStall transitions

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return AnimationDefinition.layerPostureMutator(DuckWaddle.makeAnimation(false, false), TailStall.POSTURE_MUTATOR);
	}

	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.ALLOW;
	}
	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {
		builder.add(
				new ActionTransitionDetails(
						DuckFall.ID,
						data -> !data.hasPower(Powers.TAIL_STALL),
						EvaluatorEnvironment.COMMON
				),
				DuckWaddle.UNDUCK.variate(TailStall.ID, null)
		);
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {
		builder.add(END_STALLING.variate(DuckFall.ID, null));
	}

	@Override protected ActionTransitionDetails getLandingTransition() {
		return DuckFall.DUCK_LANDING;
	}

	public static final TransitionInjectionDefinition INJECTION = new StallInjection(
			id -> id.equals(Jump.ID) || id.equals(Fall.ID),
			STALL_TRANSITION.variate(
					DUCK_STALL_ID,
					data ->
							data.hasPower(Powers.TAIL_STALL)
									&& data.getPlayer().isInSneakingPose()
									&& (data.isServer() || (
									data.getYVel() < STALL_THRESHOLD.get(data)
											&& data.getInputs().JUMP.isHeld()
							))
			)
	);
}
