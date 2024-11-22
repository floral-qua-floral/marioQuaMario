package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.definitions.actions.GroundedActionDefinition;
import com.floralquafloral.definitions.actions.CharaStat;
import com.floralquafloral.definitions.actions.StatCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DoubleJump extends Jump {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "double_jump");
	}
	@Override public @Nullable String getAnimationName() {
		return "double-jump";
	}

	public static CharaStat DOUBLE_JUMP_VEL = new CharaStat(0.939, StatCategory.JUMP_VELOCITY);
	public static CharaStat DOUBLE_JUMP_VEL_ADDEND = new CharaStat(0.08, StatCategory.JUMP_VELOCITY);
	public static CharaStat DOUBLE_JUMP_SPEED_THRESHOLD = new CharaStat(0,
			StatCategory.WALKING, StatCategory.FORWARD, StatCategory.THRESHOLD);

	@Override public List<ActionTransitionDefinition> getInputTransitions() {
		return List.of(
				AerialTransitions.GROUND_POUND,
				AerialTransitions.makeJumpCapTransition(this, 0.285)
		);
	}

	@Override
	public List<ActionTransitionDefinition> getWorldCollisionTransitions() {
		return List.of(
				CommonTransitions.ENTER_WATER,
				AerialTransitions.TRIPLE_JUMPABLE_LANDING
		);
	}

	private ActionTransitionInjection makeInjectionFrom(String otherAction) {
		return new ActionTransitionInjection(
				ActionTransitionInjection.InjectionPlacement.BEFORE,
				otherAction,
				ActionTransitionInjection.ActionCategory.GROUNDED,
				new ActionTransitionDefinition("qua_mario:double_jump",
						data ->
								data.getTimers().jumpLandingTime > 0
										&& data.getForwardVel() > DOUBLE_JUMP_SPEED_THRESHOLD.get(data)
										&& GroundedActionDefinition.GroundedTransitions.JUMP.EVALUATOR.shouldTransition(data),
						data -> GroundedActionDefinition.GroundedTransitions.performJump(data, DOUBLE_JUMP_VEL, DOUBLE_JUMP_VEL_ADDEND),
						(data, isSelf, seed) -> {
							data.playJumpSound(seed);
							data.voice(MarioClientSideData.VoiceLine.DOUBLE_JUMP, seed);
						}
				)
		);
	}

	@Override
	public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of(
			makeInjectionFrom("qua_mario:jump"),
			makeInjectionFrom("qua_mario:p_jump")
		);
	}
}
