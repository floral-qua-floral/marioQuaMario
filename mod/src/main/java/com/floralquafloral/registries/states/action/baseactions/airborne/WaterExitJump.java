package com.floralquafloral.registries.states.action.baseactions.airborne;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.definitions.actions.AquaticActionDefinition;
import com.floralquafloral.definitions.actions.CharaStat;
import com.floralquafloral.definitions.actions.GroundedActionDefinition;
import com.floralquafloral.definitions.actions.StatCategory;
import com.floralquafloral.mariodata.MarioClientSideData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WaterExitJump extends Jump {
	@Override public @NotNull Identifier getID() {
		return Identifier.of(MarioQuaMario.MOD_ID, "water_exit_jump");
	}

	public static CharaStat WATER_EXIT_JUMP_VEL = new CharaStat(0.939, StatCategory.JUMP_VELOCITY);



	@Override public List<ActionTransitionDefinition> getInputTransitions() {
		return List.of(
				AerialTransitions.GROUND_POUND
		);
	}

	@Override
	public List<ActionTransitionInjection> getTransitionInjections() {
		return List.of(
				new ActionTransitionInjection(
						ActionTransitionInjection.InjectionPlacement.BEFORE,
						"qua_mario:fall",
						ActionTransitionInjection.ActionCategory.AQUATIC,
						new ActionTransitionDefinition("qua_mario:water_exit_jump",
								data -> AquaticActionDefinition.AquaticTransitions.EXIT_WATER.EVALUATOR.shouldTransition(data)
										&& data.getYVel() > 0,
								data -> GroundedActionDefinition.GroundedTransitions.performJump(data, WATER_EXIT_JUMP_VEL, null),
								(data, isSelf, seed) -> data.playJumpSound(seed)
						)
				)
		);
	}
}
