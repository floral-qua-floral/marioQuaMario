package com.fqf.mario_qua_mario.appearances.toads.custom;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.forms.Fire;
import com.fqf.mario_qua_mario.forms.Super;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class FireCustomToadCommonAppearance extends AbstractCustomToadCommonAppearance {
	public static final Identifier ID = MarioQuaMario.makeID("fire_custom_toad");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @NotNull Identifier getFormID() {
		return Fire.ID;
	}
}
