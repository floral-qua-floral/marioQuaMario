package com.fqf.charaformact_api.appearance;

import org.joml.Vector3f;
import org.joml.Vector3i;

public record FeatureTransformationInstructions(
		float forwards, float upwards, float rightwards,
		float pitch, float yaw, float roll,
		float xScale, float yScale, float zScale
) {
	public static final FeatureTransformationInstructions VANILLA = new FeatureTransformationInstructions(
			0, 0, 0, 0, 0, 0, 0, 0, 0);

	public FeatureTransformationInstructions withPos(float forwards, float upwards, float rightwards) {
		return new FeatureTransformationInstructions(
				forwards, upwards, rightwards,
				this.pitch(), this.yaw(), this.roll(),
				this.xScale(), this.yScale(), this.zScale()
		);
	}

	public FeatureTransformationInstructions offset(float forwards, float upwards, float rightwards) {
		return this.withPos(this.forwards() + forwards, this.upwards() + upwards, this.rightwards() + rightwards);
	}

	public FeatureTransformationInstructions withAngles(float pitch, float yaw, float roll) {
		return new FeatureTransformationInstructions(
				this.forwards(), this.upwards(), this.rightwards(),
				pitch, yaw, roll,
				this.xScale(), this.yScale(), this.zScale()
		);
	}

	public FeatureTransformationInstructions rotate(float pitch, float yaw, float roll) {
		return this.withAngles(this.pitch() + pitch, this.yaw() + yaw, this.roll() + roll);
	}

	public FeatureTransformationInstructions withScale(float xScale, float yScale, float zScale) {
		return new FeatureTransformationInstructions(
				this.forwards(), this.upwards(), this.rightwards(),
				this.pitch(), this.yaw(), this.roll(),
				xScale, yScale, zScale
		);
	}

	public FeatureTransformationInstructions scale(float xScale, float yScale, float zScale) {
		return this.withScale(this.xScale() + xScale, this.yScale() + yScale, this.zScale() + zScale);
	}

	public FeatureTransformationInstructions flip(Vector3i cuboid, int vanillaHeight) {
		return this.offset(0, vanillaHeight - cuboid.y - vanillaHeight * (1 - this.yScale()), 0);
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

	public static FeatureTransformationInstructions attemptMaintainAspectRatio(Vector3i cuboid, Vector3i vanillaCuboid, int allowance, float overhangPercentage) {
		Vector3f scale = new Vector3f(cuboid).div(vanillaCuboid.x, vanillaCuboid.y, vanillaCuboid.z);
		float vanillaCuboidHeight = vanillaCuboid.y * (overhangPercentage + 1);

		// If part is just barely too small for vanilla armor, use vanilla armor size anyways
		if(scale.x < 1 && scale.x >= 1 - (float) allowance / vanillaCuboid.x) {
			if (scale.z < 1 && scale.z >= 1 - (float) allowance / vanillaCuboid.z) {
				scale.x = 1;
				scale.z = 1;
			}
		}
		if(scale.y < 1 && scale.y >= 1 - (float) allowance / vanillaCuboidHeight)
			scale.y = 1;

		// If part is just barely too small for maintained horizontal aspect ratio, maintain horizontal aspect ratio anyways
		if(scale.x < scale.z && scale.x >= scale.z - (float) allowance / vanillaCuboid.x) {
			scale.x = scale.z;
		}
		if(scale.z < scale.x && scale.z >= scale.x - (float) allowance / vanillaCuboid.z) {
			System.out.println("Hewwo! Setting aspect ratio by increasing Z scale! :3\n\tOld zScale: " +
					scale.z + "\n\txScale: " + scale.x + "\n\tallowance / cuboid.z: " + ((float) allowance / cuboid.z)
					+ "\nWhole last expression: " + (scale.x - (float) allowance / cuboid.z));
			scale.z = scale.x;
		}

		// If part is tall enough to support the Y scale matching a horizontal scale, then do that. Prefer matching X.
		if(cuboid.y * (1 + overhangPercentage) >= scale.x * vanillaCuboid.y)
			//noinspection SuspiciousNameCombination
			scale.y = scale.x;
		else if(cuboid.y * (1 + overhangPercentage) >= scale.z * vanillaCuboid.y)
			scale.y = scale.z;

		// Return the new transformation
		return new FeatureTransformationInstructions(
				0, 0, 0,
				0, 0, 0,
				scale.x, scale.y, scale.z
		);
	}

	public static FeatureTransformationInstructions attemptMaintainAspectRatioOld(Vector3i cuboid, Vector3i vanillaCuboid, int allowance, float overhangPercentage) {
		// wrote this method while extremely tired and confused
		// overhangPercentage - the feature is allowed to overhang past the part that supports it up to this percentage of its vanilla size? i think??
		Vector3f scale;
		float targetHeight = Math.max(cuboid.y, vanillaCuboid.y * (1 - overhangPercentage));
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

			if(cuboid.y * ((float) vanillaCuboid.x / targetHeight) >= horizontalSize - allowance)
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
