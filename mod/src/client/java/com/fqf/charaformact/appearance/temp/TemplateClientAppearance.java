package com.fqf.charaformact.appearance.temp;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact_api.appearance.ClientAppearanceDefinition;
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
		return CharaFormAct.makeID("textures/entity/player/uwu/template.png");
	}
}
