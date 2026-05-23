package com.fqf.mario_qua_mario.actions.grounded;

import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.PiecemealPlayermodelAnimation;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.StandUpWithKneeAnimation;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BonkGroundForward extends BonkGroundBackward implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("bonk_ground_forward");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	@Override public @Nullable PiecemealPlayermodelAnimation getOldAnimation(AnimationHelper helper) {
		return StandUpWithKneeAnimation.makeAnimation(
				helper, (data, ticksPassed) -> data.retrieveStateData(ActionTimerVars.class).actionTimer / (float) STANDUP_TICKS,
				-3.25F, 40,
				-80, -80, 90, 1.25F,
				87.5F, 0, 1.5F,
				90, 0, 0, -2
		);
	}
}
