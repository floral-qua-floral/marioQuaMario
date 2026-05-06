package com.fqf.charaformact_api.definitions.states.actions.util.animation.camera;

import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;

public record CameraProgressHandler(
		float minProgressToFinish,
		ProgressCalculator progressCalculator
) {
	public CameraProgressHandler(ProgressCalculator progressCalculator) {
		this(1, progressCalculator);
	}

	@FunctionalInterface
	public interface ProgressCalculator {
		float calculateProgress(CfaReadableMotionData data, float ticksPassed);
	}
}
