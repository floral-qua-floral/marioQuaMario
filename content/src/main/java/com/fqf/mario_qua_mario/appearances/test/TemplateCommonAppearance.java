package com.fqf.mario_qua_mario.appearances.test;

import com.fqf.charaformact_api.appearance.CommonAppearanceDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.characters.Toadette;
import com.fqf.mario_qua_mario.forms.Super;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class TemplateCommonAppearance implements CommonAppearanceDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("template");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @NotNull Identifier getCharacterID() {
		return Toadette.ID;
	}
	@Override public @NotNull Identifier getFormID() {
		return Super.ID;
	}
}
