package com.fqf.mario_qua_mario.appearances;

import com.fqf.charaformact_api.appearance.ClientAppearanceDefinition;
import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3i;

public class WonkyClientAppearance extends WonkyCommonAppearance implements ClientAppearanceDefinition {
	@Override
	public @NotNull Vector2i getTextureSize() {
		return new Vector2i(54, 92);
	}

	@Override
	public @NotNull Identifier getTextureLocation() {
		return MarioQuaMario.makeID("textures/entity/player/appearance/gradient.png");
	}

	@Override
	public Vector3i getHeadSize() {
		return new Vector3i(6, 10, 6);
	}

	@Override
	public Vector3i getTorsoSize() {
		return new Vector3i(9, 5, 6);
	}

}
