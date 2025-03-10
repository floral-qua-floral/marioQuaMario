package com.fqf.mario_qua_mario.actions.power;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.GroundPoundFlip;
import com.fqf.mario_qua_mario.actions.airborne.PJump;
import com.fqf.mario_qua_mario.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.ProgressHandler;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.powerups.Raccoon;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.Powers;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.fqf.mario_qua_mario.util.StatCategory.JUMP_VELOCITY;
import static com.fqf.mario_qua_mario.util.StatCategory.POWER_UP;

public class TailFly extends PJump implements AirborneActionDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("tail_fly");
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

	public static final CharaStat FLIGHT_VEL = new CharaStat(0.41, JUMP_VELOCITY, POWER_UP);

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new ActionTimerVars();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {
		TailStall.tailWaggleTick(data);
	}
	@Override public void travelHook(IMarioTravelData data, AirborneActionHelper helper) {
		data.getVars(Raccoon.RaccoonVars.class).flightTicks--;
		data.setYVel(FLIGHT_VEL.get(data));
		Fall.drift(data, helper);
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AirborneActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						MarioQuaMarioContent.makeID("special_fall"),
						data -> !data.hasPower(Powers.TAIL_STALL) || data.getVars(Raccoon.RaccoonVars.class).flightTicks <= 0,
						EvaluatorEnvironment.COMMON
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
		return List.of(
				GroundPoundFlip.GROUND_POUND,
				new TransitionDefinition(
						MarioQuaMarioContent.makeID("p_jump"),
						data -> !data.getInputs().JUMP.isHeld(),
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper) {
		return List.of();
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override
	public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
