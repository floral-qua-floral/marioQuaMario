package com.fqf.charaformact.util;

import com.fqf.charaformact_api.definitions.states.actions.util.animation.Arrangement;

public class AdvancedArrangement extends Arrangement {
	private float storedX, storedY, storedZ;
	private float storedPitch, storedYaw, storedRoll;

	public void storeCurrent() {
		this.storedX = this.x; this.storedY = this.y; this.storedZ = this.z;
		this.storedPitch = this.pitch; this.storedYaw = this.yaw; this.storedRoll = this.roll;
	}

	public void resetToStored() {
		this.setPos(this.storedX, this.storedY, this.storedZ);
		this.setAngles(this.storedPitch, this.storedYaw, this.storedRoll);
	}

	public void mirror() {
		float deltaX = this.x - this.storedX;
		this.x = this.storedX - deltaX;
		float deltaYaw = this.yaw - this.storedYaw;
		this.yaw = this.storedYaw - deltaYaw;
		float deltaRoll = this.roll - this.storedRoll;
		this.roll = this.storedRoll - deltaRoll;
	}
}
