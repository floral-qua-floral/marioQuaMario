package com.fqf.mario_qua_mario.definitions.states.actions.util.animation;

import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record PlayermodelAnimation(
		@NotNull ProgressCalculator progressCalculator,

		@Nullable Arrangement.Mutator wholeMutator,
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
}
