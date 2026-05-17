package com.fqf.charaformact.models.temp;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact_api.model.CharacterFormModelDefinition;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3i;

public class SmallPlayermodel implements CharacterFormModelDefinition {
	@Override public @NotNull Identifier getID() {
		return CharaFormAct.makeID("small_model");
	}
	@Override public @NotNull Identifier getCharacterID() {
		return Identifier.of("mario_qua_mario", "mario");
	}
	@Override public @NotNull Identifier getFormID() {
		return Identifier.of("mario_qua_mario", "small");
	}

	@Override
	public @NotNull Vector2i getTextureSize() {
		return new Vector2i(64, 64);
	}

	@Override
	public @NotNull Identifier getTextureLocation() {
		return CharaFormAct.makeID("textures/entity/player/uwu/gradient.png");
	}

	@Override
	public Vector3i getTorsoSize() {
		return new Vector3i(8, 4, 4);
	}

	@Override
	public Vector3i getArmSize() {
		return new Vector3i(4, 4, 4);
	}

	@Override
	public Vector3i getLegSize() {
		return new Vector3i(4, 4, 4);
	}
}
