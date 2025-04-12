package com.fqf.mario_qua_mario_content.actions.airborne;

import com.fqf.mario_qua_mario_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.ActionCategory;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_api.util.StatCategory;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class WaterExitJump extends Jump implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("water_exit_jump");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	public static final CharaStat WATER_EXIT_JUMP_VEL = new CharaStat(0.939, StatCategory.JUMP_VELOCITY);

	@Override protected double getJumpCapThreshold() {
		return Double.POSITIVE_INFINITY;
	}

	@Override
	public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of(
				new TransitionInjectionDefinition(
						TransitionInjectionDefinition.InjectionPlacement.BEFORE,
						SpecialFall.ID,
						ActionCategory.AQUATIC,
						(nearbyTransition, castableHelper) -> new TransitionDefinition(
								ID,
								data -> (data.isServer() || data.getYVel() > 0) && nearbyTransition.evaluator().shouldTransition(data),
								EvaluatorEnvironment.CLIENT_CHECKED,
								data -> {
									double waterJumpVel = WATER_EXIT_JUMP_VEL.get(data);
									if(data.getYVel() < waterJumpVel)
										castableHelper.asGrounded().performJump(data, WATER_EXIT_JUMP_VEL, null);
								},
								(data, isSelf, seed) -> data.playJumpSound(seed)
						)
				)
		);
	}
}
