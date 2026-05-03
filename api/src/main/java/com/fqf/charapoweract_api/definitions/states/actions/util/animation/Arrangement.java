package com.fqf.charapoweract_api.definitions.states.actions.util.animation;

import com.fqf.charapoweract_api.mariodata.IMarioAnimatingData;

public class Arrangement {
	public float x, y, z;
	public float pitch, yaw, roll;

	@FunctionalInterface
	public interface Mutator {
		void mutate(IMarioAnimatingData data, Arrangement arrangement, float progress);
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
