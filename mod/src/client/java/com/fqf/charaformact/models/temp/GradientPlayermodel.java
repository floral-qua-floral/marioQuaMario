package com.fqf.charaformact.models.temp;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact_api.model.CharacterFormModelDefinition;
import com.fqf.charaformact_api.model.CharacterFormModelHelper;
import net.minecraft.client.model.ModelData;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3i;

public class GradientPlayermodel implements CharacterFormModelDefinition {
	@Override public @NotNull Identifier getID() {
		return CharaFormAct.makeID("gradient");
	}
	@Override public @NotNull Identifier getCharacterID() {
		return Identifier.of("mario_qua_mario", "toadette");
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
		return CharaFormAct.makeID("textures/entity/player/uwu/gradient.png");
	}

	@Override
	public Vector3i getHeadSize() {
		return new Vector3i(6, 10, 6);
	}

	@Override
	public Vector3i getTorsoSize() {
		return new Vector3i(9, 5, 6);
	}

	@Override
	public Vector3i getArmSize() {
		return new Vector3i(6, 12*3, 2);
	}

	@Override
	public Vector3i getLegSize() {
		return new Vector3i(2, 36, 2);
	}
}
