package com.fqf.mario_qua_mario.actions.generic;

import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.EntireBodyAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.PiecemealPlayermodelAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.ProgressHandler;
import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DebugSpinPitch extends Debug {
	public static final Identifier ID = MarioQuaMario.makeID("debug_spin_pitch");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public @Nullable AnimationDefinition getAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(posture, data, animationTime, helper) -> {
					Debug.tPose(posture);
					posture.EVERYTHING.pitch = MathHelper.sin(animationTime / 40F) * MathHelper.HALF_PI;
				}
		);
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
