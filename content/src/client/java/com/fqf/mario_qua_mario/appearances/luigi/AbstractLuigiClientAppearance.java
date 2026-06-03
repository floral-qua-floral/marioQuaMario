package com.fqf.mario_qua_mario.appearances.luigi;

import com.fqf.mario_qua_mario.appearances.util.PlumberClientAppearance;
import com.fqf.mario_qua_mario.characters.Luigi;
import com.fqf.mario_qua_mario.characters.Mario;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3i;

public abstract class AbstractLuigiClientAppearance extends PlumberClientAppearance {
	@Override public @NotNull Identifier getCharacterID() {
		return Luigi.ID;
	}

	@Override public Vector3i getTorsoSize() {
		return new Vector3i(8, 11, 5);
	}

	@Override public @NotNull Vector2i getTextureSize() {
		return new Vector2i(64, 96);
	}

	@Override public Vector3i getArmSize() {
		return AbstractLuigiCommonAppearance.ARM_SIZE;
	}
}
