package com.fqf.mario_qua_mario.appearances.toads.toadette;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.appearances.toads.AbstractMiniToadCommonAppearance;
import com.fqf.mario_qua_mario.characters.Toadette;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class MiniToadetteCommonAppearance extends AbstractMiniToadCommonAppearance {
	public static final Identifier ID = MarioQuaMario.makeID("mini_toadette");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @NotNull Identifier getCharacterID() {
		return Toadette.ID;
	}
}
