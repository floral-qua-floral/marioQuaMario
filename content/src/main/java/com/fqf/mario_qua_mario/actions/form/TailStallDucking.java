package com.fqf.mario_qua_mario.actions.form;

import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.SneakingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.SprintingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.mario_qua_mario.actions.airborne.DuckFall;
import com.fqf.mario_qua_mario.actions.grounded.DuckWaddle;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.Powers;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TailStallDucking extends TailStall implements AirborneActionDefinition {
	public static final Identifier ID = DUCK_STALL_ID; // prevents warnings from trying to access subclass field in TailStall transitions
	@Override public @NotNull Identifier defineID() {
	    return ID;
	}

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
	public void accumulateBasicTransitions(ImmutableList.Builder<TransitionDefinition> builder, AirborneActionHelper helper) {
		builder.add(
				new TransitionDefinition(
						DuckFall.ID,
						data -> !data.hasPower(Powers.TAIL_STALL),
						EvaluatorEnvironment.COMMON
				),
				DuckWaddle.UNDUCK.variate(TailStall.ID, null)
		);
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<TransitionDefinition> builder, AirborneActionHelper helper) {
		builder.add(END_STALLING.variate(DuckFall.ID, null));
	}

	@Override protected TransitionDefinition getLandingTransition() {
		return DuckFall.DUCK_LANDING;
	}
}
