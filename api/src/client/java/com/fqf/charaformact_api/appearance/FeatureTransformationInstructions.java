package com.fqf.charaformact_api.appearance;

import org.joml.Vector3f;
import org.joml.Vector3i;

public record FeatureTransformationInstructions(
		float forwards, float upwards, float rightwards,
		float pitch, float yaw, float roll,
		float xScale, float yScale, float zScale
) {
	public FeatureTransformationInstructions flip(Vector3i cuboid, int vanillaHeight) {
		return new FeatureTransformationInstructions(
				this.forwards(),
				this.upwards() + vanillaHeight - cuboid.y - vanillaHeight * (1 - this.yScale()),
				this.rightwards(),
				this.pitch(), this.yaw(), this.roll(),
				this.xScale(), this.yScale(), this.zScale()
		);
	}

	public static FeatureTransformationInstructions stretchToCover(Vector3i cuboid, Vector3i vanillaCuboid) {
		return new FeatureTransformationInstructions(
				1, 1, 1,
				0, 0, 0,
				(float) cuboid.x / vanillaCuboid.x,
				(float) cuboid.y / vanillaCuboid.y,
				(float) cuboid.y / vanillaCuboid.y
		);
	}

	public static FeatureTransformationInstructions attemptMaintainAspectRatio(Vector3i cuboid, Vector3i vanillaCuboid, int allowance, float targetHeightFactor) {
		// wrote this method while extremely tired and confused
		Vector3f scale;
		float targetHeight = Math.max(cuboid.y, vanillaCuboid.y * targetHeightFactor);
		if(Math.abs(cuboid.x - cuboid.z) <= allowance) {
			// We can preserve the horizontal aspect ratio, so maybe we can keep the vertical ratio intact too!
			int horizontalSize = Math.max(cuboid.x, cuboid.z);
			int vanillaHorizontalSize = Math.max(vanillaCuboid.x, vanillaCuboid.z);
			float horizontalScale;
			if(Math.abs(horizontalSize - Math.max(vanillaCuboid.x, vanillaCuboid.z)) <= allowance) {
				horizontalSize = vanillaHorizontalSize;
				horizontalScale = 1;
			}
			else
				horizontalScale = (float) horizontalSize / vanillaCuboid.x;

			if(cuboid.y * ((float) vanillaCuboid.y / vanillaCuboid.x) >= horizontalSize - allowance)
				scale = new Vector3f(horizontalScale, horizontalScale, horizontalScale);
			else
				scale = new Vector3f(horizontalScale, (float) cuboid.y / targetHeight, horizontalScale);
		}
		else scale = new Vector3f(
				(float) cuboid.x / vanillaCuboid.x,
				cuboid.y / targetHeight,
				(float) cuboid.z / vanillaCuboid.z
		);

		return new FeatureTransformationInstructions(
				0, 0, 0,
				0, 0, 0,
				scale.x, scale.y, scale.z
		);
	}
}
