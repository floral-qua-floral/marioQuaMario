package com.fqf.mario_qua_mario.appearances.toads.toadette;

import com.fqf.mario_qua_mario.appearances.toads.AbstractToadClientAppearance;
import com.fqf.mario_qua_mario.characters.Toadette;
import com.fqf.mario_qua_mario.forms.Raccoon;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class RaccoonToadetteClientAppearance extends AbstractToadClientAppearance {
	@Override
	public @NotNull Identifier getID() {
		return RaccoonToadetteCommonAppearance.ID;
	}

	@Override
	public @NotNull Identifier getCharacterID() {
		return Toadette.ID;
	}

	@Override
	public @NotNull Identifier getFormID() {
		return Raccoon.ID;
	}
}
