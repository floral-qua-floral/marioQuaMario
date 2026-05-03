package com.fqf.charapoweract_api.definitions.states.actions.util.animation.camera;

import com.fqf.charapoweract_api.mariodata.IMarioReadableMotionData;

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
