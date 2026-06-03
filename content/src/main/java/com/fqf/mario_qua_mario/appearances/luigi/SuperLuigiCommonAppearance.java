package com.fqf.mario_qua_mario.appearances.luigi;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.forms.Super;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class SuperLuigiCommonAppearance extends AbstractLuigiCommonAppearance {
	public static final Identifier ID = MarioQuaMario.makeID("super_luigi");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	@Override public @NotNull Identifier getFormID() {
		return Super.ID;
	}
}
