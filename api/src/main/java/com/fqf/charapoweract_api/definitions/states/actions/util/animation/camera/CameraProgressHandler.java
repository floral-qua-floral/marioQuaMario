package com.fqf.charapoweract_api.definitions.states.actions.util.animation.camera;

import com.fqf.charapoweract_api.cpadata.ICPAReadableMotionData;

public record CameraProgressHandler(
		float minProgressToFinish,
		ProgressCalculator progressCalculator
) {
	public CameraProgressHandler(ProgressCalculator progressCalculator) {
		this(1, progressCalculator);
	}

	@FunctionalInterface
	public interface ProgressCalculator {
		float calculateProgress(ICPAReadableMotionData data, float ticksPassed);
	}
}
