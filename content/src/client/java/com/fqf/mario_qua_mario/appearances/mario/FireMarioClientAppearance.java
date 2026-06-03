package com.fqf.mario_qua_mario.appearances.mario;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.forms.Fire;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public class FireMarioClientAppearance extends AbstractMarioClientAppearance {
	public static final Identifier ID = MarioQuaMario.makeID("fire_mario");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public @NotNull Identifier getFormID() {
		return Fire.ID;
	}
}
