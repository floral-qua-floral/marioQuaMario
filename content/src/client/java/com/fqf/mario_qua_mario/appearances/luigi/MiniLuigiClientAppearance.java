package com.fqf.mario_qua_mario.appearances.luigi;

import com.fqf.mario_qua_mario.appearances.mario.MiniMarioClientAppearance;
import com.fqf.mario_qua_mario.characters.Luigi;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class MiniLuigiClientAppearance extends MiniMarioClientAppearance {
	@Override public @NotNull Identifier getID() {
		return MiniLuigiCommonAppearance.ID;
	}

	@Override public @NotNull Identifier getCharacterID() {
		return Luigi.ID;
	}
}
