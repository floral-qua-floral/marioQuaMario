package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.SneakingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.SprintingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.grounded.DuckWaddle;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DuckJump extends Jump implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("duck_jump");


	@Override
	public @Nullable AnimationDefinition defineAnimation() {
		return DuckWaddle.makeAnimation(false, false);
	}

	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.ALLOW;
	}
	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	public static ActionTransitionDetails makeDuckJumpTransition(GroundedActionDefinition.GroundedActionHelper helper) {
		return new ActionTransitionDetails(
				DuckJump.ID,
				data -> data.getInputs().JUMP.isPressed(),
				EvaluatorEnvironment.CLIENT_ONLY,
				data -> {
					helper.performJump(data, JUMP_VEL, JUMP_ADDEND);
					data.getInputs().DUCK.isPressed(); // Unbuffer DUCK
				},
				(data, isSelf, seed) -> {
					data.playJumpSound(seed);
					data.voice(Voicelines.DUCK_JUMP, seed);
				}
		);
	}

	@Override protected double getJumpCapThreshold() {
		return 0.14;
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {
		builder.add(DuckWaddle.UNDUCK.variate(Jump.ID, null));
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {
		builder.add(helper.makeJumpCapTransition(this.getJumpCapThreshold()));
	}

	@Override protected ActionTransitionDetails getLandingTransition() {
		return DuckFall.DUCK_LANDING;
	}
}
