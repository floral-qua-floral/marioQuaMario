package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioClientSideData;
import com.floralquafloral.definitions.actions.GroundedActionDefinition;
import com.floralquafloral.definitions.actions.CharaStat;
import com.floralquafloral.definitions.actions.StatCategory;
import com.floralquafloral.util.Easings;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TripleJump extends Jump {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "triple_jump");
	}
	@Override public @Nullable String getAnimationName() {
		return "triple-jump";
	}
	@Override @Nullable public CameraAnimationSet getCameraAnimations() {
		return new CameraAnimationSet(
				new CameraAnimation(
						false, 0.85F,
						(progress, offsets) -> offsets[1] = Easings.easeOutQuad(progress) * 360
				),
				null,
				null
		);
	}

	public static CharaStat TRIPLE_JUMP_VEL = new CharaStat(1.175, StatCategory.JUMP_VELOCITY);
	public static CharaStat TRIPLE_JUMP_SPEED_THRESHOLD = new CharaStat(0.34,
			StatCategory.RUNNING, StatCategory.FORWARD, StatCategory.THRESHOLD);

	@Override public List<ActionTransitionDefinition> getPostTickTransitions() {
		return List.of(
				AerialTransitions.GROUND_POUND,
				AerialTransitions.makeJumpCapTransition(this, 0.65)
		);
	}

	private ActionTransitionInjection makeInjectionFrom(String otherAction) {
		return new ActionTransitionInjection(
				ActionTransitionInjection.InjectionPlacement.BEFORE,
				otherAction,
				ActionTransitionInjection.ActionCategory.GROUNDED,
				new ActionTransitionDefinition("qua_mario:triple_jump",
						data ->
								data.getTimers().doubleJumpLandingTime > 0
								&& data.getForwardVel() > TRIPLE_JUMP_SPEED_THRESHOLD.get(data)
								&& GroundedActionDefinition.GroundedTransitions.JUMP.EVALUATOR.shouldTransition(data),
						data -> GroundedActionDefinition.GroundedTransitions.performJump(data, TRIPLE_JUMP_VEL, null),
						(data, isSelf, seed) -> {
							data.playJumpSound(seed);
							data.voice(MarioClientSideData.VoiceLine.TRIPLE_JUMP, seed);
						}
				)
		);
	}

	@Override
	public List<ActionTransitionDefinition> getPostMoveTransitions() {
		return List.of(
				AerialTransitions.BASIC_LANDING
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
