package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.actions.grounded.DuckWaddle;
import com.fqf.mario_qua_mario.actions.grounded.PRun;
import com.fqf.mario_qua_mario.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario.definitions.states.actions.util.SneakingRule;
import com.fqf.mario_qua_mario.definitions.states.actions.util.SprintingRule;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario.util.Easing;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PJump extends Jump implements AirborneActionDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("p_jump");
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null, null, null,
				null, null,
				new LimbAnimation(false, (data, arrangement, progress) -> {
					arrangement.roll += 90;
					arrangement.pitch += 15;
				}),
				new LimbAnimation(false, (data, arrangement, progress) -> {
					arrangement.roll -= 90;
					arrangement.pitch += 15;
				}),

				new LimbAnimation(false, (data, arrangement, progress) -> arrangement.pitch += 40),
				new LimbAnimation(false, (data, arrangement, progress) -> arrangement.pitch += 40),
				null
		);
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.ALLOW;
	}

	@Override
	protected double getJumpCapThreshold() {
		return 0.4;
	}

	@Override
	public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper) {
		return List.of(
				Jump.DOUBLE_JUMPABLE_LANDING.variate(MarioQuaMarioContent.makeID("p_run"), data ->
						Fall.LANDING.evaluator().shouldTransition(data) && (!data.isClient() || PRun.meetsPRunRequirements(data))),
				Jump.DOUBLE_JUMPABLE_LANDING
		);
	}
}
