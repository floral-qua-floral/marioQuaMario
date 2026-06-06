package com.fqf.mario_qua_mario.appearances.toads.custom;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.appearances.toads.AbstractSmallToadCommonAppearance;
import com.fqf.mario_qua_mario.characters.CustomToad;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class SmallCustomToadCommonAppearance extends AbstractSmallToadCommonAppearance {
	public static final Identifier ID = MarioQuaMario.makeID("small_custom_toad");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @NotNull Identifier getCharacterID() {
		return CustomToad.ID;
	}
}
