package com.fqf.mario_qua_mario.definitions.states.actions.util.animation.camera;

import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;

public record CameraProgressHandler(
		float minProgressToFinish,
		ProgressCalculator progressCalculator
) {
	public CameraProgressHandler(ProgressCalculator progressCalculator) {
		this(1, progressCalculator);
	}

	@FunctionalInterface
	public interface ProgressCalculator {
		float calculateProgress(IMarioReadableMotionData data, float ticksPassed);
	}
}
