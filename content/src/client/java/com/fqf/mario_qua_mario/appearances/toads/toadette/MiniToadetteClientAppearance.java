package com.fqf.mario_qua_mario.appearances.toads.toadette;

import com.fqf.mario_qua_mario.appearances.toads.AbstractMiniToadClientAppearance;
import com.fqf.mario_qua_mario.characters.Toadette;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class MiniToadetteClientAppearance extends AbstractMiniToadClientAppearance {
	@Override
	public @NotNull Identifier getID() {
		return MiniToadetteCommonAppearance.ID;
	}

	@Override
	public @NotNull Identifier getCharacterID() {
		return Toadette.ID;
	}
}
