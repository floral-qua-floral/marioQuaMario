package com.fqf.mario_qua_mario.definitions.states.actions.util.animation;

import com.fqf.mario_qua_mario.mariodata.IMarioReadableMotionData;

public class Arrangement {
	public float x, y, z;
	public float pitch, yaw, roll;

	public Arrangement(float x, float y, float z, float pitch, float yaw, float roll) {
		this.setPos(x, y, z);
		this.setAngles(pitch, yaw, roll);
	}
	public Arrangement(float pitch, float yaw, float roll) {
		this(0, 0, 0, pitch, yaw, roll);
	}

	@FunctionalInterface
	public interface Mutator {
		void mutate(IMarioReadableMotionData data, Arrangement arrangement, float progress);
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
