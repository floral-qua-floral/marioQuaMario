package com.fqf.mario_qua_mario.appearances.toads;

import org.joml.Vector3i;

public abstract class AbstractSmallToadCommonAppearance extends AbstractToadCommonAppearance {
	public static Vector3i ARM_SIZE = new Vector3i(3, 6, 4);
	public static Vector3i LEG_SIZE = new Vector3i(4, 4, 4);

	@Override public Vector3i getArmSize() {
		return ARM_SIZE;
	}
	@Override public Vector3i getLegSize() {
		return LEG_SIZE;
	}

}
