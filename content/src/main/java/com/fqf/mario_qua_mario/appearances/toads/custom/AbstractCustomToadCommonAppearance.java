package com.fqf.mario_qua_mario.appearances.toads.custom;

import com.fqf.mario_qua_mario.appearances.toads.AbstractToadCommonAppearance;
import com.fqf.mario_qua_mario.characters.CustomToad;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractCustomToadCommonAppearance extends AbstractToadCommonAppearance {
	@Override public @NotNull Identifier getCharacterID() {
		return CustomToad.ID;
	}
}
