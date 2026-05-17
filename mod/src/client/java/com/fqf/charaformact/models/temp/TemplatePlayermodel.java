package com.fqf.charaformact.models.temp;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact_api.model.CharacterFormModelDefinition;
import com.fqf.charaformact_api.model.CharacterFormModelHelper;
import net.minecraft.client.model.ModelData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;

public class TemplatePlayermodel implements CharacterFormModelDefinition {
	@Override public @NotNull Identifier getID() {
		return CharaFormAct.makeID("test_model");
	}
	@Override public @NotNull Identifier getCharacterID() {
		return Identifier.of("mario_qua_mario", "mario");
	}
	@Override public @NotNull Identifier getFormID() {
		return Identifier.of("mario_qua_mario", "super");
	}

	@Override
	public @NotNull Vector2i getTextureSize() {
		return new Vector2i(64, 64);
	}

	@Override
	public @NotNull Identifier getTextureLocation() {
		return CharaFormAct.makeID("textures/entity/player/uwu/template.png");
	}
}
