package com.fqf.mario_qua_mario.appearances.luigi;

import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import com.fqf.mario_qua_mario.characters.Luigi;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

public abstract class AbstractLuigiCommonAppearance implements CommonAppearanceDefinition {
	public static final Vector3i ARM_SIZE = new Vector3i(3, 12, 4);

	@Override public @NotNull Identifier getCharacterID() {
		return Luigi.ID;
	}
	@Override public Vector3i getArmSize() {
		return ARM_SIZE;
	}
}
