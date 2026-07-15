package com.fqf.charaformact.appearance;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.registries.RegistryManager;
import com.fqf.charaformact.registries.power_granting.AppearanceKey;
import com.fqf.charaformact_api.CharaFormActClientAddon;
import com.fqf.charaformact_api.appearance.AppearanceGeometryHelper;
import com.fqf.charaformact_api.appearance.ClientAppearanceDefinition;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.Map;

public class ClientAppearanceCollector extends AbstractAppearanceCollector<ClientAppearanceDefinition, Pair<ParsedClientAppearance, AppearanceRenderer>> {
	public static ClientAppearanceCollector INSTANCE = new ClientAppearanceCollector();

	private @Nullable ParsedClientAppearance currentlyInitializingAppearance;
	public @Nullable ParsedClientAppearance getCurrentlyInitializingAppearance() {
		return this.currentlyInitializingAppearance;
	}

	@Override
	protected Map<AppearanceKey.Registerable, ClientAppearanceDefinition> getDefinitions() {
		AppearanceMapBuilderImpl<ClientAppearanceDefinition> builder = new AppearanceMapBuilderImpl<>();
		for(CharaFormActClientAddon addon : RegistryManager.getEntrypoints("charaformact-client", CharaFormActClientAddon.class)) {
			addon.accumulateClientAppearances(builder);
		}
		return builder.build();
	}

	@Override
	protected Pair<ParsedClientAppearance, AppearanceRenderer> parse(AppearanceKey.Registerable key, ClientAppearanceDefinition definition) {
		EntityModelLayer layer = new EntityModelLayer(key.ID, "main");
		EntityModelLayerRegistry.registerModelLayer(layer, () -> getTexturedModelDataFor(definition));
		return new Pair<>(new ParsedClientAppearance(key, layer, definition), null);
	}

	@Override
	protected ParsedCommonAppearance refine(Pair<ParsedClientAppearance, AppearanceRenderer> from) {
		return from.getLeft();
	}

	private static TexturedModelData getTexturedModelDataFor(ClientAppearanceDefinition definition) {
		Vector2i textureSize = definition.defineTextureSize();
		ModelData modelData = definition.getModelData(AppearanceHelperImpl.INSTANCE);

		ModelPartData modelRoot = modelData.getRoot();

		// Add deadmau5's stupid ear which is required or else the game will crash
		if(modelRoot.getChild("ear") == null)
			AppearanceHelperImpl.INSTANCE.makeInvisiblePart(modelRoot, "ear", new Vector3f(), false);

		// Add cape with default geometry. Its positioning will be handled using the feature rendering system.
		if(modelRoot.getChild("cloak") == null)
			AppearanceHelperImpl.INSTANCE.makeInvisiblePart(modelRoot, AppearanceGeometryHelper.CAPE, new Vector3f(), false);

		return TexturedModelData.of(modelData, textureSize.x, textureSize.y);
	}

	public void reloadAppearanceRenderers(EntityRendererFactory.Context ctx) {
		try {
			this.map.forEach((combo, pair) -> {
				try {
					ParsedClientAppearance model = pair.getLeft();
					this.currentlyInitializingAppearance = model;
					AppearanceRenderer renderer = new AppearanceRenderer(ctx, model);
					pair.setRight(renderer);
				} catch(Exception exception) {
					throw new IllegalArgumentException("Failed to create player model for " + combo, exception);
				}
			});
		}
		finally {
			this.currentlyInitializingAppearance = null;
		}
	}

	@Override
	public void collect() {
		super.collect();
		CommonAppearanceCollector.INSTANCE.validate(this.validationMap);
		// Clear client-side validation map, we don't need it anymore!
		this.validationMap = null;
	}

	public <T extends LivingEntity, M extends EntityModel<T>> void captureFeature(FeatureRenderer<T, M> feature) {
//		this.capturedFeatures.add((FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>) feature);
		for(Pair<ParsedClientAppearance, AppearanceRenderer> pair : this.map.values()) {
			CharaFormAct.LOGGER.info("Distributing a feature to Appearance Renderer {}...", pair.getLeft().ID);
			//noinspection unchecked
			pair.getRight().addCapturedFeature((FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>) feature);
		}
	}
}
