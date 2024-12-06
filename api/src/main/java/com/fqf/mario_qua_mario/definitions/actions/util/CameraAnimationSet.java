package com.fqf.mario_qua_mario.definitions.actions.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CameraAnimationSet {
	public CameraAnimationSet(
			@NotNull CameraAnimation authentic,
			@Nullable CameraAnimation gentle,
			@Nullable CameraAnimation minimal
	) {
		this.AUTHENTIC_ANIMATION = authentic;
		this.GENTLE_ANIMATION = gentle == null ? authentic : gentle;
		this.MINIMAL_ANIMATION = minimal;
	}

	public static class CameraAnimation {
		@FunctionalInterface
		public interface rotationalOffsetCalculator {
			void setRotationalOffsets(float progress, float[] offsets);
		}

		public CameraAnimation(
				boolean looping,
				float durationSeconds,
				CameraAnimation.rotationalOffsetCalculator calculator
		) {
			this.SHOULD_LOOP = looping;
			this.DURATION_TICKS = durationSeconds * 20;
			this.CALCULATOR = calculator;
		}

		public final boolean SHOULD_LOOP;
		public final float DURATION_TICKS;
		public final CameraAnimation.rotationalOffsetCalculator CALCULATOR;
	}

	@NotNull
	public final CameraAnimation AUTHENTIC_ANIMATION;
	@NotNull
	public final CameraAnimation GENTLE_ANIMATION;
	@Nullable
	public final CameraAnimation MINIMAL_ANIMATION;
}
