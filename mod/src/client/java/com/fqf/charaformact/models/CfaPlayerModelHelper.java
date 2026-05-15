package com.fqf.charaformact.models;

import com.fqf.charaformact.CharaFormActClient;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class CfaPlayerModelHelper {
	public static final CharacterFormCombo UWU = new CharacterFormCombo();

	private static @Nullable CharacterFormEntityModel customModelForRenderer;

//	public static final Map<Pair<ParsedCharacter, ParsedForm>, EntityRendererFactory<AbstractClientPlayerEntity>> MODELS;
	private static Set<CharacterFormCombo> combos;
	private static Map<CharacterFormCombo, EntityRenderer<AbstractClientPlayerEntity>> renderers;

	public static void registerCharacterFormCombos() {
		combos = Set.of(UWU);
	}

	public static void reloadCustomPlayerRenderers(EntityRendererFactory.Context ctx) {
		try {
			ImmutableMap.Builder<CharacterFormCombo, EntityRenderer<AbstractClientPlayerEntity>> builder = ImmutableMap.builder();
			for(CharacterFormCombo combo : combos) {
				try {
					customModelForRenderer = new CfaTestModel(ctx.getPart(CharaFormActClient.TEST_LAYER));
					builder.put(combo, new CustomPlayerEntityRenderer(ctx));
				} catch (Exception var5) {
					throw new IllegalArgumentException("Failed to create player model for " + combo.getName(), var5);
				}
			}
			renderers = builder.build();
		}
		finally {
			customModelForRenderer = null;
		}
	}

	public static @Nullable EntityRenderer<AbstractClientPlayerEntity> getRenderer(AbstractClientPlayerEntity player) {
		return renderers.get(UWU);
	}

	public static @Nullable CharacterFormEntityModel getCustomModelForRenderer() {
		return customModelForRenderer;
	}
	public static boolean isMakingCustomRenderer() {
		return getCustomModelForRenderer() != null;
	}
}
