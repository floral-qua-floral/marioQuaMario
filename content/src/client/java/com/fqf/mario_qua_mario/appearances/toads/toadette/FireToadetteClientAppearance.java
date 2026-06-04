package com.fqf.mario_qua_mario.appearances.toads.toadette;

import com.fqf.mario_qua_mario.appearances.toads.AbstractToadClientAppearance;
import com.fqf.mario_qua_mario.characters.Toadette;
import com.fqf.mario_qua_mario.forms.Fire;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class FireToadetteClientAppearance extends AbstractToadClientAppearance {
	@Override
	public @NotNull Identifier getID() {
		return FireToadetteCommonAppearance.ID;
	}

	@Override
	public @NotNull Identifier getCharacterID() {
		return Toadette.ID;
	}

	@Override
	public @NotNull Identifier getFormID() {
		return Fire.ID;
	}
}
