package com.fqf.mario_qua_mario.appearances.luigi;

import com.fqf.mario_qua_mario.appearances.util.PlumberClientAppearance;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3i;

public class SuperLuigiClientAppearance extends PlumberClientAppearance {
	@Override public Vector3i getArmSize() {
		return SuperLuigiCommonAppearance.ARM_SIZE;
	}
	@Override public Vector3i getTorsoSize() {
		return new Vector3i(8, 11, 5);
	}

	@Override public @NotNull Vector2i defineTextureSize() {
		return new Vector2i(64, 96);
	}

}
