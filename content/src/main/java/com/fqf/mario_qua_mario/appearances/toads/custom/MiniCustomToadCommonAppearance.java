package com.fqf.mario_qua_mario.appearances.toads.custom;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.appearances.toads.AbstractMiniToadCommonAppearance;
import com.fqf.mario_qua_mario.characters.CustomToad;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class MiniCustomToadCommonAppearance extends AbstractMiniToadCommonAppearance {
	public static final Identifier ID = MarioQuaMario.makeID("mini_custom_toad");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @NotNull Identifier getCharacterID() {
		return CustomToad.ID;
	}
}
