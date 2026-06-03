package com.fqf.mario_qua_mario.appearances.luigi;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.forms.Raccoon;
import com.fqf.mario_qua_mario.forms.Super;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class FoxLuigiCommonAppearance extends AbstractLuigiCommonAppearance {
	public static final Identifier ID = MarioQuaMario.makeID("fox_luigi");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	@Override public @NotNull Identifier getFormID() {
		return Raccoon.ID;
	}
}
