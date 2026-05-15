package com.fqf.charaformact.models;

import com.google.common.collect.ImmutableMap;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CfaPlayerModelHelper {
	public static final CharacterFormCombo UWU = new CharacterFormCombo();

	private static @Nullable CharacterFormCombo modellingCombo = null;

//	public static final Map<Pair<ParsedCharacter, ParsedForm>, EntityRendererFactory<AbstractClientPlayerEntity>> MODELS;
	public static final Map<CharacterFormCombo, EntityRendererFactory<AbstractClientPlayerEntity>> MODELS;
	private static Map<CharacterFormCombo, EntityRenderer<AbstractClientPlayerEntity>> renderers;

	static {
		// TODO: Iterate through custom playermodels for realsies
		MODELS = Map.of(
				UWU,
				context -> new PlayerEntityRenderer(context, false)
		);
	}

	public static void reloadCustomPlayerRenderers(EntityRendererFactory.Context ctx) {
		try {
			ImmutableMap.Builder<CharacterFormCombo, EntityRenderer<AbstractClientPlayerEntity>> builder = ImmutableMap.builder();
			modellingCombo = UWU;
			try {
				builder.put(modellingCombo, new CustomPlayerEntityRenderer(ctx));
			} catch (Exception var5) {
				throw new IllegalArgumentException("Failed to create player model for " + modellingCombo.getName(), var5);
			}
			renderers = builder.build();
		}
		finally {
			modellingCombo = null;
		}

	}

	public static @Nullable EntityRenderer<AbstractClientPlayerEntity> getRenderer(AbstractClientPlayerEntity player) {
		return renderers.get(UWU);
	}

	public static CharacterFormCombo getModellingCharacterForm() {
		return modellingCombo;
	}
	public static boolean isMakingCustomRenderer() {
		return getModellingCharacterForm() != null;
	}
}
