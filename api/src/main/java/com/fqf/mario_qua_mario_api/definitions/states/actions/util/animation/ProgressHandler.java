package com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation;

import com.fqf.mario_qua_mario_api.mariodata.IMarioAnimatingData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario_api.util.Easing;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ProgressHandler(@Nullable Identifier animationID, @Nullable ProgressResetPredicate resetter, @NotNull ProgressCalculator calculator) {
	public ProgressHandler(ProgressCalculator calculator) {
		this(null, null, calculator);
	}

	public ProgressHandler(float duration, boolean looping, Easing easing) {
		this(null, null, looping
				? (data, ticksPassed) -> easing.ease((ticksPassed / duration) % 1)
				: (data, ticksPassed) -> easing.ease(Math.min(ticksPassed / duration, 1)));
	}

	@FunctionalInterface
	public interface ProgressCalculator {
		float calculateProgress(IMarioAnimatingData data, int ticksPassed);
	}

	@FunctionalInterface
	public interface ProgressResetPredicate {
		boolean shouldReset(IMarioAnimatingData data, @Nullable Identifier prevAnimationID);
	}
}
