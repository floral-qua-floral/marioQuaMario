package com.fqf.mario_qua_mario.actions.generic;

import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.EntireBodyAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.PiecemealPlayermodelAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.ProgressHandler;
import com.fqf.charaformact_api.util.Easing;
import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DebugSpinRoll extends Debug {
	public static final Identifier ID = MarioQuaMario.makeID("debug_spin_roll");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public @Nullable PiecemealPlayermodelAnimation getOldAnimation(AnimationHelper helper) {
		return new PiecemealPlayermodelAnimation(
				null, new ProgressHandler(40F, true, Easing.LINEAR),
				new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) ->
						arrangement.roll = progress * 360),

				null, null,
				null, null,
				null, null,
				null
		);
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
