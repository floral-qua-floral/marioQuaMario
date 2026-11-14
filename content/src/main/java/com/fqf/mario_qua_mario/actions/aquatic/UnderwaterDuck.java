package com.fqf.mario_qua_mario.actions.aquatic;

import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.grounded.DuckWaddle;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public class UnderwaterDuck implements AquaticActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("underwater_duck");

	@Override public float defineHitboxHeight() {
		return DuckWaddle.DUCK_HEIGHT;
	}

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return DuckWaddle.makeAnimation(true, false);
	}

	@Override public void defineActionFlags(ImmutableSet.Builder<ActionFlag> builder) {
		builder.addAll(ActionFlag.SNEAKING);
		builder.addAll(ActionFlag.SLIDING);
	}

	@Override public void travelHook(CfaTravelData data, AquaticActionHelper helper) {
		Submerged.waterMove(data, helper);
	}

	public static final ActionTransitionDetails DUCK_SUBMERGE = UnderwaterWalk.SUBMERGE.variate(UnderwaterDuck.ID, null);

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AquaticActionHelper helper) {
		builder.add(DuckWaddle.UNDUCK.variate(UnderwaterWalk.ID, null));
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AquaticActionHelper helper) {
		builder.add(
				Swim.SWIM.variate(null, null, null, null, data -> {
					assert Swim.SWIM.travelExecutor() != null;
					Swim.SWIM.travelExecutor().execute(data);
					data.getInputs().DUCK.isPressed(); // Force unbuffer DUCK
				}, null)
		);
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AquaticActionHelper helper) {
		builder.add(
				UnderwaterWalk.EXIT_WATER.variate(DuckWaddle.ID, null),
				Fall.FALL.variate(Submerged.ID, null)
		);
	}
}