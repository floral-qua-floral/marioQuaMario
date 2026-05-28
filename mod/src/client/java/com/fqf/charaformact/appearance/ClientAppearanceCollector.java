package com.fqf.charaformact.appearance;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.registries.power_granting.CharacterFormCombo;
import com.fqf.charaformact.registries.power_granting.ParsedCharacter;
import com.fqf.charaformact.registries.power_granting.ParsedForm;
import com.fqf.charaformact_api.appearance.AppearanceGeometryHelper;
import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.fqf.charaformact_api.appearance.ClientAppearanceDefinition;
import com.google.common.collect.ImmutableMap;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class ClientAppearanceCollector extends AbstractAppearanceCollector<ClientAppearanceDefinition, Pair<ParsedClientAppearance, AppearanceRenderer>> {
	public static ClientAppearanceCollector INSTANCE = new ClientAppearanceCollector();

	private @Nullable AppearanceModel customModelForRenderer;
	public @Nullable AppearanceModel getCustomModelForRenderer() {
		return this.customModelForRenderer;
	}

	@Override protected String getEntrypoint() {
		return "cfa-client-appearances";
	}

	@Override protected Class<ClientAppearanceDefinition> getEntrypointClass() {
		return ClientAppearanceDefinition.class;
	}

	@Override
	protected Pair<ParsedClientAppearance, AppearanceRenderer> parse(ClientAppearanceDefinition definition, ParsedCharacter character, ParsedForm form) {
		EntityModelLayerRegistry.registerModelLayer(definition.getModelLayer(), () -> getTexturedModelDataFor(definition));
		return new Pair<>(new ParsedClientAppearance(definition, character, form), null);
	}

	private static TexturedModelData getTexturedModelDataFor(ClientAppearanceDefinition definition) {
		Vector2i textureSize = definition.getTextureSize();
		ModelData modelData = definition.getModelData(AppearanceHelperImpl.INSTANCE);

		if(CharaFormAct.CONFIG.logCharacterFormModelUVs()) {
			// i'm sorry this code is unbearably ugly but i just mashed it together for a quick test and i don't wanna rewrite it
			AppearanceGeometryHelper helper = AppearanceHelperImpl.INSTANCE;
			Vector2i headCorner = helper.getUVDimensions(definition.getHeadSize());
			Vector2i hatCorner = helper.getUVDimensions(definition.getHeadSize());
			Vector2i torsoCorner = helper.getUVDimensions(definition.getTorsoSize());
			Vector2i jacketCorner = helper.getUVDimensions(definition.getTorsoSize());
			Vector2i rightLegCorner = helper.getUVDimensions(definition.getLegSize());
			Vector2i rightPantsCorner = helper.getUVDimensions(definition.getLegSize());
			Vector2i rightArmCorner = helper.getUVDimensions(definition.getArmSize());
			Vector2i rightSleeveCorner = helper.getUVDimensions(definition.getArmSize());
			CharaFormAct.LOGGER.info("""
				{}'s vanilla part UV information:
				\tHead UV @ {}, {}  ->  {}, {}
				\tHat UV @ {}, {}  ->  {}, {}
				\tTorso UV @ {}, {}  ->  {}, {}
				\tJacket UV @ {}, {}  ->  {}, {}
				\tLeg UV @ {}, {}  ->  {}, {}
				\tPants UV @ {}, {}  ->  {}, {}
				\tArm UV @ {}, {}  ->  {}, {}
				\tSleeve UV @ {}, {}  ->  {}, {}""",
					definition.getID(),
					definition.getHeadUV().x, definition.getHeadUV().y, headCorner.x, headCorner.y,
					definition.getHatUV(helper).x, definition.getHatUV(helper).y, hatCorner.x, hatCorner.y,
					definition.getTorsoUV(helper).x, definition.getTorsoUV(helper).y, torsoCorner.x, torsoCorner.y,
					definition.getJacketUV(helper).x, definition.getJacketUV(helper).y, jacketCorner.x, jacketCorner.y,
					definition.getRightLegUV(helper).x, definition.getRightLegUV(helper).y, rightLegCorner.x, rightLegCorner.y,
					definition.getRightPantsUV(helper).x, definition.getRightPantsUV(helper).y, rightPantsCorner.x, rightPantsCorner.y,
					definition.getRightArmUV(helper).x, definition.getRightArmUV(helper).y, rightArmCorner.x, rightArmCorner.y,
					definition.getRightSleeveUV(helper).x, definition.getRightSleeveUV(helper).y, rightSleeveCorner.x, rightSleeveCorner.y
			);
		}

		ModelPartData modelRoot = modelData.getRoot();

		// Add deadmau5's stupid ear which is required or else the game will crash
		if(modelRoot.getChild("ear") == null)
			AppearanceHelperImpl.INSTANCE.makeInvisiblePart(modelRoot, "ear", new Vector3f(), false);

		// Add cape with default geometry. Its positioning will be handled using the feature rendering system.
		if(modelRoot.getChild("cloak") == null) AppearanceHelperImpl.INSTANCE.makePart(
				modelRoot, AppearanceGeometryHelper.CAPE, false,
				new Vector3f(), new Vector3f(-5, 0, -1),
				0, new Vector3f(), new Vector3i(10, 16, 1), new Vector2i()
		);

		return TexturedModelData.of(modelData, textureSize.x, textureSize.y);
	}

	public void reloadAppearanceRenderers(EntityRendererFactory.Context ctx) {
		try {
			this.map.forEach((combo, pair) -> {
				try {
					ParsedClientAppearance model = pair.getLeft();
					this.customModelForRenderer = model.makeAndGetModel(ctx);
					AppearanceRenderer renderer = new AppearanceRenderer(ctx, model.TEXTURE_LOCATION);
					pair.setRight(renderer);
				} catch(Exception exception) {
					throw new IllegalArgumentException("Failed to create player model for " + combo, exception);
				}
			});
		}
		finally {
			this.customModelForRenderer = null;
		}
	}

	@Override
	public void collect() {
		super.collect();
		ImmutableMap.Builder<CharacterFormCombo, Identifier> collectedIDs = ImmutableMap.builderWithExpectedSize(this.map.size());
		this.map.forEach((combo, pair) -> collectedIDs.put(combo, pair.getLeft().ID));
		CommonAppearanceCollector.INSTANCE.validate(collectedIDs.build());
	}
}
