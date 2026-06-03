package com.fqf.mario_qua_mario.appearances.mario;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.MarioQuaMarioClient;
import com.fqf.mario_qua_mario.appearances.util.PlumberClientAppearance;
import com.fqf.mario_qua_mario.characters.Mario;
import com.fqf.mario_qua_mario.forms.Super;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3i;

public class SuperMarioClientAppearance extends AbstractMarioClientAppearance {
	public static final Identifier ID = MarioQuaMario.makeID("super_mario");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	@Override public @NotNull Identifier getFormID() {
		return Super.ID;
	}
}
