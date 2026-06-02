package com.fqf.mario_qua_mario.appearances.toads.toadette;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.forms.Raccoon;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class RaccoonToadetteCommonAppearance extends AbstractToadetteCommonAppearance {
	public static final Identifier ID = MarioQuaMario.makeID("raccoon_toadette");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public @NotNull Identifier getFormID() {
		return Raccoon.ID;
	}
}
