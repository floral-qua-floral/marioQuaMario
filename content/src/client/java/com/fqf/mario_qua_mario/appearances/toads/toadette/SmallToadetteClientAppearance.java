package com.fqf.mario_qua_mario.appearances.toads.toadette;

import com.fqf.mario_qua_mario.appearances.toads.AbstractSmallToadClientAppearance;
import com.fqf.mario_qua_mario.characters.Toadette;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class SmallToadetteClientAppearance extends AbstractSmallToadClientAppearance {
	@Override
	public @NotNull Identifier getID() {
		return SmallToadetteCommonAppearance.ID;
	}

	@Override
	public @NotNull Identifier getCharacterID() {
		return Toadette.ID;
	}
}
