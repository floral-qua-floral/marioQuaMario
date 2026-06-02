package com.fqf.mario_qua_mario.appearances.toads.toadette;

import com.fqf.mario_qua_mario.appearances.toads.AbstractToadClientAppearance;
import com.fqf.mario_qua_mario.characters.Toadette;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractToadetteClientAppearance extends AbstractToadClientAppearance {
	@Override public @NotNull Identifier getCharacterID() {
		return Toadette.ID;
	}

}
