package com.fqf.charaformact_api.model;

public record FeatureTransformationInstructions(
		float backwards, float downwards, float leftwards,
		float pitch, float yaw, float roll,
		float xScale, float yScale, float zScale
) {
}
