package com.fqf.mario_qua_mario.appearances.luigi;

import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import org.joml.Vector3i;

public class SuperLuigiCommonAppearance implements CommonAppearanceDefinition {
	public static final Vector3i ARM_SIZE = new Vector3i(3, 12, 4);

	@Override public Vector3i getArmSize() {
		return ARM_SIZE;
	}
}
