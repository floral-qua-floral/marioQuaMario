package com.fqf.mario_qua_mario.appearances.toads;

import org.joml.Vector3i;

public abstract class AbstractMiniToadCommonAppearance extends AbstractToadCommonAppearance {
	public static Vector3i ARM_SIZE = new Vector3i(1, 3, 2);
	public static Vector3i LEG_SIZE = new Vector3i(2, 2, 2);

	@Override public Vector3i getArmSize() {
		return ARM_SIZE;
	}
	@Override public Vector3i getLegSize() {
		return LEG_SIZE;
	}

}
