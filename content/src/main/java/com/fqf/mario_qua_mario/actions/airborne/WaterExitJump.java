package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.definitions.TransitionInjectionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.StatCategory;
import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.util.Identifier;

public class WaterExitJump extends Jump implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("water_exit_jump");

	public static final CfaStat WATER_EXIT_JUMP_VEL = new CfaStat(0.939, StatCategory.JUMP_VELOCITY);

	@Override protected double getJumpCapThreshold() {
		return Double.POSITIVE_INFINITY;
	}

	public static final TransitionInjectionDefinition INJECTION = new TransitionInjectionDefinition.Simple(
			TransitionInjectionDefinition.InjectionPlacement.BEFORE, SpecialFall.ID,
			ActionCategory.AQUATIC,
			(nearbyTransition, castableHelper) -> new ActionTransitionDetails(
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
	);
}
