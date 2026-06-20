package com.fqf.mario_qua_mario.actions.generic;

import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DebugSpinPitch extends Debug {
	public static final Identifier ID = MarioQuaMario.makeID("debug_spin_pitch");
	@Override public @NotNull Identifier defineID() {
	    return ID;
	}

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(arrangement, data, animationTime, helper) -> arrangement.pitch = MathHelper.sin(animationTime / 4F) * 35,
				(posture, data, animationTime, helper) -> Debug.tPose(posture)
		);
	}

	@Override public void accumulateAttackInterceptions(ImmutableList.Builder<AttackInterceptionDefinition> builder, AnimationHelper helper) {

	}
}
