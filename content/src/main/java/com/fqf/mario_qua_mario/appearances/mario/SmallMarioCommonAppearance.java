package com.fqf.mario_qua_mario.appearances.mario;

import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.util.Identifier;
import org.joml.Vector3i;

public class SmallMarioCommonAppearance implements CommonAppearanceDefinition {
	public static final Vector3i ARM_SIZE = new Vector3i(4, 5, 4);
	public static final Vector3i LEG_SIZE = new Vector3i(4, 5, 4);

	public static final Identifier ID = MarioQuaMario.makeID("small_mario");

	@Override public Vector3i getArmSize() {
		return ARM_SIZE;
	}
	@Override public Vector3i getLegSize() {
		return LEG_SIZE;
	}
}
