package com.fqf.charaformact_api.appearance;

import org.joml.Vector3f;
import org.joml.Vector3i;

public record TransformationInstructions(
		float forwards, float upwards, float rightwards,
		float pitch, float yaw, float roll,
		float xScale, float yScale, float zScale
) {
	public static final TransformationInstructions VANILLA = new TransformationInstructions(
			0, 0, 0, 0, 0, 0, 1, 1, 1);

	public TransformationInstructions withPos(float forwards, float upwards, float rightwards) {
		return new TransformationInstructions(
				forwards, upwards, rightwards,
				this.pitch(), this.yaw(), this.roll(),
				this.xScale(), this.yScale(), this.zScale()
		);
	}

	public TransformationInstructions offset(float forwards, float upwards, float rightwards) {
		return this.withPos(this.forwards() + forwards, this.upwards() + upwards, this.rightwards() + rightwards);
	}

	public TransformationInstructions withAngles(float pitch, float yaw, float roll) {
		return new TransformationInstructions(
				this.forwards(), this.upwards(), this.rightwards(),
				pitch, yaw, roll,
				this.xScale(), this.yScale(), this.zScale()
		);
	}

	public TransformationInstructions rotate(float pitch, float yaw, float roll) {
		return this.withAngles(this.pitch() + pitch, this.yaw() + yaw, this.roll() + roll);
	}

	public TransformationInstructions withScale(float xScale, float yScale, float zScale) {
		return new TransformationInstructions(
				this.forwards(), this.upwards(), this.rightwards(),
				this.pitch(), this.yaw(), this.roll(),
				xScale, yScale, zScale
		);
	}

	public TransformationInstructions scale(float xScale, float yScale, float zScale) {
		return this.withScale(this.xScale() + xScale, this.yScale() + yScale, this.zScale() + zScale);
	}

	public TransformationInstructions flip(Vector3i cuboid, int vanillaHeight) {
		return this.offset(0, vanillaHeight - cuboid.y - vanillaHeight * (1 - this.yScale()), 0);
	}
}
