package com.fqf.charaformact.models;

import com.fqf.charaformact.CharaFormActClient;
import com.fqf.charaformact_api.model.CharacterFormModelDefinition;
import com.google.common.collect.ImmutableMap;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

public class CfaPlayerModelHelper {
	public static final CharacterFormCombo UWU = new CharacterFormCombo(1);
	public static final CharacterFormCombo WEELOO = new CharacterFormCombo(3);

	private static @Nullable CharacterFormEntityModel customModelForRenderer;

//	public static final Map<Pair<ParsedCharacter, ParsedForm>, EntityRendererFactory<AbstractClientPlayerEntity>> MODELS;
	private static Set<CharacterFormCombo> combos;
	private static Map<CharacterFormCombo, EntityRenderer<AbstractClientPlayerEntity>> renderers;

	public static void registerCharacterFormCombos() {
		combos = Set.of(UWU, WEELOO);
	}

	public static void reloadCustomPlayerRenderers(EntityRendererFactory.Context ctx) {
		try {
			ImmutableMap.Builder<CharacterFormCombo, EntityRenderer<AbstractClientPlayerEntity>> builder = ImmutableMap.builder();
			for(CharacterFormCombo combo : combos) {
				try {
					CharacterFormModelDefinition def = combo == UWU ? CharaFormActClient.TEST_MODEL : CharaFormActClient.GRADIENT_MODEL;
					customModelForRenderer = new CfaTestModel(ctx.getPart(def.getModelLayer()));
					builder.put(combo, new CharacterFormRenderer(ctx, def.getTextureLocation()));
				} catch(Exception exception) {
					throw new IllegalArgumentException("Failed to create player model for " + combo.getName(), exception);
				}
			}
			renderers = builder.build();
		}
		finally {
			customModelForRenderer = null;
		}
	}

	public static @Nullable EntityRenderer<AbstractClientPlayerEntity> getRenderer(AbstractClientPlayerEntity player) {
		if(player.isSneaking()) return renderers.get(WEELOO);
		return renderers.get(UWU);
	}

	public static @Nullable CharacterFormEntityModel getCustomModelForRenderer() {
		return customModelForRenderer;
	}
	public static boolean isMakingCustomRenderer() {
		return getCustomModelForRenderer() != null;
	}
}
