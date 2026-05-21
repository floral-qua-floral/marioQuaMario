package com.fqf.mario_qua_mario.appearances;

import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.characters.Mario;
import com.fqf.mario_qua_mario.forms.Fire;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3i;

public class AlexCommonAppearance implements CommonAppearanceDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("alex");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @NotNull Identifier getCharacterID() {
		return Mario.ID;
	}
	@Override public @NotNull Identifier getFormID() {
		return Fire.ID;
	}

	@Override public Vector3i getArmSize() {
		return new Vector3i(3, 12, 4);
	}
}
