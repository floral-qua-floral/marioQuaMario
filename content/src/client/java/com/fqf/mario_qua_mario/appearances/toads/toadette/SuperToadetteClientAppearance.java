package com.fqf.mario_qua_mario.appearances.toads.toadette;

import com.fqf.mario_qua_mario.appearances.toads.AbstractToadClientAppearance;
import com.fqf.mario_qua_mario.forms.Super;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class SuperToadetteClientAppearance extends AbstractToadetteClientAppearance {
	@Override public @NotNull Identifier getID() {
		return SuperToadetteCommonAppearance.ID;
	}

	@Override public @NotNull Identifier getFormID() {
		return Super.ID;
	}
}
