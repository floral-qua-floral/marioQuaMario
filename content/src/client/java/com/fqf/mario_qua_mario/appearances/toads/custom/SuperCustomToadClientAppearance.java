package com.fqf.mario_qua_mario.appearances.toads.custom;

import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.fqf.mario_qua_mario.appearances.toads.SuperToadClientAppearance;
import com.fqf.mario_qua_mario.appearances.util.CustomizableTextureLayerFeature;
import com.fqf.mario_qua_mario.appearances.util.CustomizableToadAppearanceModel;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.Identifier;

public class SuperCustomToadClientAppearance extends SuperToadClientAppearance {
	private final CustomizableTextureLayerFeature.SpotsMode SPOTS_MODE;

	public SuperCustomToadClientAppearance(CustomizableTextureLayerFeature.SpotsMode spotsMode) {
		this.SPOTS_MODE = spotsMode;
	}

	@Override
	public AppearanceModel createModel(Identifier appearanceID, ModelPart root) {
		return new CustomizableToadAppearanceModel(appearanceID, root);
	}

	@Override
	public void accumulateCustomFeatureRenderers(Identifier texture, ImmutableList.Builder<FeatureRenderer<AbstractClientPlayerEntity, AppearanceModel>> builder, FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> featureRendererContext, EntityRendererFactory.Context ctx) {
		builder.addAll(CustomizableTextureLayerFeature.makeCustomToadFeatures(featureRendererContext, texture, "super", this.SPOTS_MODE));
	}
}
