package com.fqf.mario_qua_mario.definitions.states.actions.util.animation;

import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ProgressHandler(@Nullable Identifier animationID, @Nullable ProgressResetPredicate resetter, @NotNull ProgressCalculator calculator) {
	public ProgressHandler(ProgressCalculator calculator) {
		this(null, null, calculator);
	}

	public ProgressHandler(float duration, boolean looping) {
		this(null, null, looping
				? (data, ticksPassed) -> (ticksPassed / duration) % 1
				: (data, ticksPassed) -> Math.min(ticksPassed / duration, 1));
	}

	@FunctionalInterface
	public interface ProgressCalculator {
		float calculateProgress(IMarioReadableMotionData data, int ticksPassed);
	}

	@FunctionalInterface
	public interface ProgressResetPredicate {
		boolean shouldReset(IMarioReadableMotionData data, @Nullable Identifier prevAnimationID);
	}
}
