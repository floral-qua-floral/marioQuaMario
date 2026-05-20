package com.fqf.mario_qua_mario.appearances;

import com.fqf.charaformact_api.appearance.ClientAppearanceDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3i;

public class SmallClientAppearance extends SmallCommonAppearance implements ClientAppearanceDefinition {

	@Override
	public @NotNull Vector2i getTextureSize() {
		return new Vector2i(64, 64);
	}

	@Override
	public @NotNull Identifier getTextureLocation() {
		return MarioQuaMario.makeID("textures/entity/player/appearance/template_small.png");
	}

	@Override
	public Vector3i getTorsoSize() {
		return new Vector3i(8, 4, 4);
	}

}
