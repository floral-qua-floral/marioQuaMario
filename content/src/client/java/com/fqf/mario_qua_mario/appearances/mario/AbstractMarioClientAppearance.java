package com.fqf.mario_qua_mario.appearances.mario;

import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.fqf.mario_qua_mario.MarioQuaMarioClient;
import com.fqf.mario_qua_mario.appearances.util.PlumberClientAppearance;
import com.fqf.mario_qua_mario.characters.Mario;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector3i;

import java.util.List;

public abstract class AbstractMarioClientAppearance extends PlumberClientAppearance {
	@Override public @NotNull Identifier getCharacterID() {
		return Mario.ID;
	}

	@Override public Vector3i getLegSize() {
		return AbstractMarioCommonAppearance.LEG_SIZE;
	}
	@Override public Vector3i getTorsoSize() {
		return new Vector3i(8, 11, 6);
	}

	@Override public @NotNull Vector2i getTextureSize() {
		return new Vector2i(64, 96);
	}
}
