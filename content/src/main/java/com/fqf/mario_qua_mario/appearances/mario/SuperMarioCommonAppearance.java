package com.fqf.mario_qua_mario.appearances.mario;

import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import org.joml.Vector3i;

public class SuperMarioCommonAppearance implements CommonAppearanceDefinition {
	public static final Vector3i LEG_SIZE = new Vector3i(4, 11, 4);

	@Override public Vector3i getLegSize() {
		return LEG_SIZE;
	}
}
