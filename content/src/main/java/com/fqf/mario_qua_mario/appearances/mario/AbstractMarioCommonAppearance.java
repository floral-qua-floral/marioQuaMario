package com.fqf.mario_qua_mario.appearances.mario;

import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import com.fqf.mario_qua_mario.characters.Mario;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

public abstract class AbstractMarioCommonAppearance implements CommonAppearanceDefinition {
	public static final Vector3i LEG_SIZE = new Vector3i(4, 11, 4);

	@Override public @NotNull Identifier getCharacterID() {
		return Mario.ID;
	}
	@Override public Vector3i getLegSize() {
		return LEG_SIZE;
	}
}
