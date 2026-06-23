package com.fqf.mario_qua_mario.appearances.toads;

import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import org.joml.Vector3i;

public class SuperToadCommonAppearance implements CommonAppearanceDefinition {
	public static Vector3i ARM_SIZE = new Vector3i(3, 9, 4);
	public static Vector3i LEG_SIZE = new Vector3i(4, 8, 4);

	@Override public Vector3i getArmSize() {
		return ARM_SIZE;
	}
	@Override public Vector3i getLegSize() {
		return LEG_SIZE;
	}
}
