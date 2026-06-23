package com.fqf.mario_qua_mario.appearances.test;

import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.util.Identifier;
import org.joml.Vector3i;

public class WonkyCommonAppearance implements CommonAppearanceDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("wonky");

	@Override public Vector3i getArmSize() {
		return new Vector3i(6, 12 * 3, 2);
	}
	@Override public Vector3i getLegSize() {
		return new Vector3i(2, 36, 2);
	}
}
