package com.fqf.mario_qua_mario.actions.form;

import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.ProgressHandler;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
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
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.JUMP_VELOCITY;
import static com.fqf.charaformact_api.util.StatCategory.FORM;

public class TailFly extends PJump implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("tail_fly");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return Objects.requireNonNull(super.getAnimation(helper)).variate(
			null,
			new ProgressHandler((data, ticksPassed) -> ticksPassed * 1.1F),
			null, null, null,
			null, null,
			null, null,
			TailStall.makeTailAnimation(true)
		);
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

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AirborneActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						SpecialFall.ID,
						data -> !data.hasPower(Powers.TAIL_STALL) || data.retrieveStateData(Raccoon.RaccoonVars.class).flightTicks <= 0,
						EvaluatorEnvironment.COMMON
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
		return List.of(
				GroundPoundFlip.GROUND_POUND,
				new TransitionDefinition(
						PJump.ID,
						data -> !data.getInputs().JUMP.isHeld(),
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper) {
		return List.of(
				Submerged.SUBMERGE
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
