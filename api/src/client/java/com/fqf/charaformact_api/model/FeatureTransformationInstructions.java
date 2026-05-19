package com.fqf.charaformact_api.model;

public record FeatureTransformationInstructions(
		float forwards, float upwards, float rightwards,
		float pitch, float yaw, float roll,
		float xScale, float yScale, float zScale
) {
}
