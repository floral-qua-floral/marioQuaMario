package com.fqf.mario_qua_mario.definitions.states.actions.util.animation;

import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PlayermodelAnimation(
		@Nullable PlayermodelAnimation.MirroringEvaluator mirroringEvaluator,
		@NotNull ProgressCalculator progressCalculator,

		@Nullable EntireBodyAnimation entireBodyAnimation,
		@Nullable BodyPartAnimation headAnimation,
		@Nullable BodyPartAnimation torsoAnimation,

		@Nullable LimbAnimation rightArmAnimation,
		@Nullable LimbAnimation leftArmAnimation,

		@Nullable LimbAnimation rightLegAnimation,
		@Nullable LimbAnimation leftLegAnimation,

		@Nullable BodyPartAnimation capeAnimation
) {

	@FunctionalInterface
	public interface ProgressCalculator {
		float calculateProgress(IMarioReadableMotionData data, int ticksPassed);
	}

	@FunctionalInterface
	public interface MirroringEvaluator {
		boolean shouldMirror(IMarioReadableMotionData data, boolean rightArmBusy, boolean leftArmBusy, float headRelativeYaw);
	}
}
