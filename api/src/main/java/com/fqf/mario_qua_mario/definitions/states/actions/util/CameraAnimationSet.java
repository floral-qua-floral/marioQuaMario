package com.fqf.mario_qua_mario.definitions.states.actions.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record CameraAnimationSet(
		@NotNull CameraAnimation authentic,
		@Nullable CameraAnimation gentle,
		@Nullable CameraAnimation minimal
) {
	public record CameraAnimation(
			boolean looping,
			float durationSeconds,
			CameraAnimation.rotationalOffsetCalculator calculator
	) {
		@FunctionalInterface
		public interface rotationalOffsetCalculator {
			void setRotationalOffsets(float progress, CameraOffsets offsets);
		}

		public static class CameraOffsets {
			public float pitch, yaw, roll;
			public float x, y, z;
		}
	}
}
