package com.fqf.mario_qua_mario.appearances.test;

import com.fqf.charaformact_api.appearance.ClientAppearanceDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public class TemplateClientAppearance extends TemplateCommonAppearance implements ClientAppearanceDefinition {
	@Override
	public @NotNull Vector2i getTextureSize() {
		return new Vector2i(64, 64);
	}

	@Override
	public @NotNull Identifier getTextureLocation() {
		return MarioQuaMario.makeID("textures/entity/player/appearance/template.png");
	}
}
