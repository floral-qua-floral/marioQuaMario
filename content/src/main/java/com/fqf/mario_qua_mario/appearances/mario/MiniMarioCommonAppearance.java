package com.fqf.mario_qua_mario.appearances.mario;

import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import org.joml.Vector3i;

public class MiniMarioCommonAppearance implements CommonAppearanceDefinition {
	public static final Vector3i ARM_SIZE = new Vector3i(2, 2, 2);
	public static final Vector3i LEG_SIZE = new Vector3i(2, 2, 2);

	@Override public Vector3i getArmSize() {
		return ARM_SIZE;
	}
	@Override public Vector3i getLegSize() {
		return LEG_SIZE;
	}
}
