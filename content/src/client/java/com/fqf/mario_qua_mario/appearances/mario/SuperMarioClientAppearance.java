package com.fqf.mario_qua_mario.appearances.mario;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.MarioQuaMarioClient;
import com.fqf.mario_qua_mario.appearances.util.PlumberClientAppearance;
import com.fqf.mario_qua_mario.characters.Mario;
import com.fqf.mario_qua_mario.forms.Super;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3i;

public class SuperMarioClientAppearance extends PlumberClientAppearance {
	public static final Identifier ID = MarioQuaMario.makeID("super_mario");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	@Override public @NotNull Identifier getCharacterID() {
		return Mario.ID;
	}
	@Override public @NotNull Identifier getFormID() {
		return Super.ID;
	}

	@Override public @NotNull Vector2i getTextureSize() {
		return new Vector2i(64, 96);
	}
	@Override public @NotNull Identifier getTextureLocation() {
		return MarioQuaMarioClient.makeAppearanceTextureID(this);
	}

	@Override
	public Vector3i getLegSize() {
		return SuperMarioCommonAppearance.LEG_SIZE;
	}

	@Override
	public Vector3i getTorsoSize() {
		return new Vector3i(8, 11, 6);
	}
}
