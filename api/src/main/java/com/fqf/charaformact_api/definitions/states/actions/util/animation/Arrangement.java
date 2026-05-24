package com.fqf.charaformact_api.definitions.states.actions.util.animation;

import com.fqf.charaformact_api.cfadata.CfaAnimatingData;

public class Arrangement {
	public float x, y, z;
	public float pitch, yaw, roll;

	@FunctionalInterface
	public interface Mutator {
		void mutate(CfaAnimatingData data, Arrangement arrangement, float progress);
	}

	public void setPos(float x, float y, float z) {
		this.x = x; this.y = y; this.z = z;
	}
	public void setAngles(float pitch, float yaw, float roll) {
		this.pitch = pitch; this.yaw = yaw; this.roll = roll;
	}

	public void addPos(float x, float y, float z) {
		this.setPos(this.x + x, this.y + y, this.z + z);
	}
	public void addAngles(float pitch, float yaw, float roll) {
		this.setAngles(this.pitch + pitch, this.yaw + yaw, this.roll + roll);
	}
}
