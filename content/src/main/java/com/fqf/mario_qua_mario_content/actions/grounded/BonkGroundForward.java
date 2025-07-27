package com.fqf.mario_qua_mario_content.actions.grounded;

import com.fqf.mario_qua_mario_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.util.Easing;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.util.ActionTimerVars;
import com.fqf.mario_qua_mario_content.util.StandUpWithKneeAnimation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BonkGroundForward extends BonkGroundBackward implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("bonk_ground_forward");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return StandUpWithKneeAnimation.getAnimation(
				helper, (data, ticksPassed) -> data.getVars(ActionTimerVars.class).actionTimer / (float) STANDUP_TICKS,
				-3.25F, 40,
				-80, -80, 90, 1.25F,
				87.5F, 0, 1.5F,
				90, 0, 0, -2
		);
	}
}
