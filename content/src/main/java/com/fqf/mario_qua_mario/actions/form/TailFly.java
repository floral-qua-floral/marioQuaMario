package com.fqf.mario_qua_mario.actions.form;

import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.GroundPoundFlip;
import com.fqf.mario_qua_mario.actions.airborne.PJump;
import com.fqf.mario_qua_mario.actions.airborne.SpecialFall;
import com.fqf.mario_qua_mario.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario.forms.Raccoon;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.Powers;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.fqf.charaformact_api.util.StatCategory.FORM;
import static com.fqf.charaformact_api.util.StatCategory.JUMP_VELOCITY;

public class TailFly extends PJump implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("tail_fly");

	@Override public @NotNull AnimationDefinition defineAnimation() {
		return AnimationDefinition.layerPostureMutator(super.defineAnimation(), TailStall.POSTURE_MUTATOR);
	}

	public static final CfaStat FLIGHT_VEL = new CfaStat(0.41, JUMP_VELOCITY, FORM);

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new ActionTimerVars();
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {
		TailStall.tailWaggleTick(data);
	}
	@Override public void travelHook(CfaTravelData data, AirborneActionHelper helper) {
		data.retrieveStateData(Raccoon.RaccoonVars.class).flightTicks--;
		data.setYVel(FLIGHT_VEL.get(data));
		Fall.drift(data, helper);
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {
		builder.add(new ActionTransitionDetails(
				SpecialFall.ID,
				data -> !data.hasPower(Powers.TAIL_STALL) || data.retrieveStateData(Raccoon.RaccoonVars.class).flightTicks <= 0,
				EvaluatorEnvironment.COMMON
		));
	}

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {
		builder.add(
				GroundPoundFlip.GROUND_POUND,
				new ActionTransitionDetails(
						PJump.ID,
						data -> !data.getInputs().JUMP.isHeld(),
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {
		builder.add(Submerged.SUBMERGE);
	}
}
