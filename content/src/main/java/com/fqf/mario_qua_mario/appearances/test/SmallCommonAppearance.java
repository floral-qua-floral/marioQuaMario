package com.fqf.mario_qua_mario.appearances.test;

import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.characters.Toadette;
import com.fqf.mario_qua_mario.forms.Small;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

public class SmallCommonAppearance implements CommonAppearanceDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("small");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @NotNull Identifier getCharacterID() {
		return Toadette.ID;
	}
	@Override public @NotNull Identifier getFormID() {
		return Small.ID;
	}

	@Override public Vector3i getArmSize() {
		return new Vector3i(4, 4, 4);
	}
	@Override public Vector3i getLegSize() {
		return new Vector3i(4, 4, 4);
	}
}
