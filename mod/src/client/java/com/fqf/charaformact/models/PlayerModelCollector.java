package com.fqf.charaformact.models;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.registries.power_granting.CharacterFormCombo;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact_api.model.CharacterFormEntityModel;
import com.fqf.charaformact_api.model.CharacterFormModelDefinition;
import com.fqf.charaformact_api.model.CharacterFormModelHelper;
import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.Map;

public class PlayerModelCollector {
	private static Map<CharacterFormCombo, ParsedCharacterFormModel> parsedModels;
	private static Map<CharacterFormCombo, Pair<ParsedCharacterFormModel, PlayerEntityRenderer>> modelsAndRenderers;

	private static @Nullable CharacterFormEntityModel customModelForRenderer;

	public static void parseModelDefinitions() {
		ImmutableMap.Builder<CharacterFormCombo, ParsedCharacterFormModel> builder = ImmutableMap.builder();

		for(CharacterFormModelDefinition definition : RegistryManager.getEntrypoints("cfa-character-form-models", CharacterFormModelDefinition.class)) {
			Identifier modelID = definition.getID();
			Identifier characterID = definition.getCharacterID();
			Identifier formID = definition.getFormID();
			ParsedCharacter character = RegistryManager.CHARACTERS.get(definition.getCharacterID());
			ParsedForm form = RegistryManager.FORMS.get(definition.getFormID());

			if(character == null && form == null)
				CharaFormAct.LOGGER.warn("Model {}'s character ({}) and form ({}) are both unregistered??", modelID, characterID, formID);
			else if(character == null)
				CharaFormAct.LOGGER.info("Model {}'s character ({}) is unregistered, ignoring...", modelID, characterID);
			else if(form == null)
				CharaFormAct.LOGGER.info("Model {}'s form ({}) is unregistered, ignoring...", modelID, formID);
			else {
				CharaFormAct.LOGGER.info("Found a playermodel with ID {}, for the character {} in the form {}. Yay!",
						modelID, characterID, formID);
				EntityModelLayerRegistry.registerModelLayer(definition.getModelLayer(), () -> getTexturedModelDataFor(definition));
				builder.put(new CharacterFormCombo(character, form), new ParsedCharacterFormModel(definition, character, form));
			}
		};

		parsedModels = builder.build();
	}

	private static TexturedModelData getTexturedModelDataFor(CharacterFormModelDefinition definition) {
		Vector2i textureSize = definition.getTextureSize();
		ModelData modelData = definition.getModelData(CharacterFormModelHelperImpl.INSTANCE);

		if(CharaFormAct.CONFIG.logCharacterFormModelUVs()) {
			// i'm sorry this code is unbearably garbage but i just mashed it together for a quick test and i don't wanna rewrite it
			CharacterFormModelHelper helper = CharacterFormModelHelperImpl.INSTANCE;
			CharaFormAct.LOGGER.info("""
				{}'s UV information:
				\tHead UV @ {}, {}  ->  {}, {}
				\tHat UV @ {}, {}  ->  {}, {}
				\tTorso UV @ {}, {}  ->  {}, {}
				\tJacket UV @ {}, {}  ->  {}, {}
				\tLeg UV @ {}, {}  ->  {}, {}
				\tPants UV @ {}, {}  ->  {}, {}
				\tArm UV @ {}, {}  ->  {}, {}
				\tSleeve UV @ {}, {}  ->  {}, {}""",
					definition.getID(),
					definition.getHeadUV().x, definition.getHeadUV().y,
					helper.getBottomRightCorner(definition.getHeadUV(), definition.getHeadSize()).x,
					helper.getBottomRightCorner(definition.getHeadUV(), definition.getHeadSize()).y,
					definition.getHatUV(helper).x, definition.getHatUV(helper).y,
					helper.getBottomRightCorner(definition.getHatUV(helper), definition.getHeadSize()).x,
					helper.getBottomRightCorner(definition.getHatUV(helper), definition.getHeadSize()).y,
					definition.getTorsoUV(helper).x, definition.getTorsoUV(helper).y,
					helper.getBottomRightCorner(definition.getTorsoUV(helper), definition.getTorsoSize()).x,
					helper.getBottomRightCorner(definition.getTorsoUV(helper), definition.getTorsoSize()).y,
					definition.getJacketUV(helper).x, definition.getJacketUV(helper).y,
					helper.getBottomRightCorner(definition.getJacketUV(helper), definition.getTorsoSize()).x,
					helper.getBottomRightCorner(definition.getJacketUV(helper), definition.getTorsoSize()).y,
					definition.getRightLegUV(helper).x, definition.getRightLegUV(helper).y,
					helper.getBottomRightCorner(definition.getRightLegUV(helper), definition.getLegSize()).x,
					helper.getBottomRightCorner(definition.getRightLegUV(helper), definition.getLegSize()).y,
					definition.getRightPantsUV(helper).x, definition.getRightPantsUV(helper).y,
					helper.getBottomRightCorner(definition.getRightPantsUV(helper), definition.getLegSize()).x,
					helper.getBottomRightCorner(definition.getRightPantsUV(helper), definition.getLegSize()).y,
					definition.getRightArmUV(helper).x, definition.getRightArmUV(helper).y,
					helper.getBottomRightCorner(definition.getRightArmUV(helper), definition.getArmSize()).x,
					helper.getBottomRightCorner(definition.getRightArmUV(helper), definition.getArmSize()).y,
					definition.getRightSleeveUV(helper).x, definition.getRightSleeveUV(helper).y,
					helper.getBottomRightCorner(definition.getRightSleeveUV(helper), definition.getArmSize()).x,
					helper.getBottomRightCorner(definition.getRightSleeveUV(helper), definition.getArmSize()).y
			);
		}

		// Add deadmau5's stupid ear which is required or else the game will crash
		if(modelData.getRoot().getChild("ear") == null)
			CharacterFormModelHelperImpl.INSTANCE.makeInvisiblePart(modelData.getRoot(), "ear", new Vector3f(), false);

		return TexturedModelData.of(modelData, textureSize.x, textureSize.y);
	}

	public static void reloadCustomPlayerRenderers(EntityRendererFactory.Context ctx) {
		try {
			ImmutableMap.Builder<CharacterFormCombo, Pair<ParsedCharacterFormModel, PlayerEntityRenderer>> builder = ImmutableMap.builder();
			for(Map.Entry<CharacterFormCombo, ParsedCharacterFormModel> entry : parsedModels.entrySet()) {
				try {
					ParsedCharacterFormModel model = entry.getValue();
					customModelForRenderer = model.makeAndGetModel(ctx);
					PlayerEntityRenderer renderer = new CharacterFormRenderer(ctx, model.TEXTURE_LOCATION);
					builder.put(entry.getKey(), new Pair<>(model, renderer));
				} catch(Exception exception) {
					throw new IllegalArgumentException("Failed to create player model for " + entry.getKey(), exception);
				}
			}
			modelsAndRenderers = builder.build();
		}
		finally {
			customModelForRenderer = null;
		}
	}

	public static @Nullable Pair<ParsedCharacterFormModel, PlayerEntityRenderer> getModelAndRenderer(CharacterFormCombo combo) {
		return modelsAndRenderers.get(combo);
	}

	public static @Nullable CharacterFormEntityModel getCustomModelForRenderer() {
		return customModelForRenderer;
	}
	public static boolean isMakingCustomRenderer() {
		return getCustomModelForRenderer() != null;
	}
}
