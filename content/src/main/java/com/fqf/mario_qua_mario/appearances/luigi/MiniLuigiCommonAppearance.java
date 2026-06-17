package com.fqf.mario_qua_mario.appearances.luigi;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.appearances.mario.MiniMarioCommonAppearance;
import com.fqf.mario_qua_mario.characters.Luigi;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class MiniLuigiCommonAppearance extends MiniMarioCommonAppearance {
	public static final Identifier ID = MarioQuaMario.makeID("mini_luigi");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public @NotNull Identifier getCharacterID() {
		return Luigi.ID;
	}
}
