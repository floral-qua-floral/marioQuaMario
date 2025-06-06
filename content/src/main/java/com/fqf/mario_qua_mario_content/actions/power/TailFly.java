package com.fqf.mario_qua_mario_content.actions.power;

import com.fqf.mario_qua_mario_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.ProgressHandler;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.airborne.Fall;
import com.fqf.mario_qua_mario_content.actions.airborne.GroundPoundFlip;
import com.fqf.mario_qua_mario_content.actions.airborne.PJump;
import com.fqf.mario_qua_mario_content.actions.airborne.SpecialFall;
import com.fqf.mario_qua_mario_content.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario_content.powerups.Raccoon;
import com.fqf.mario_qua_mario_content.util.ActionTimerVars;
import com.fqf.mario_qua_mario_content.util.Powers;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.fqf.mario_qua_mario_api.util.StatCategory.JUMP_VELOCITY;
import static com.fqf.mario_qua_mario_api.util.StatCategory.POWER_UP;

public class TailFly extends PJump implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("tail_fly");
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
						SpecialFall.ID,
						data -> !data.hasPower(Powers.TAIL_STALL) || data.getVars(Raccoon.RaccoonVars.class).flightTicks <= 0,
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
