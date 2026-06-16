package com.fqf.mario_qua_mario.appearances.toads.custom;

import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.fqf.mario_qua_mario.appearances.toads.AbstractToadClientAppearance;
import com.fqf.mario_qua_mario.appearances.util.CustomizableTextureLayerFeature;
import com.fqf.mario_qua_mario.appearances.util.CustomizableToadAppearanceModel;
import com.fqf.mario_qua_mario.characters.CustomToad;
import com.fqf.mario_qua_mario.forms.Fire;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FireCustomToadClientAppearance extends AbstractToadClientAppearance {
	@Override
	public @NotNull Identifier getID() {
		return FireCustomToadCommonAppearance.ID;
	}

	@Override
	public @NotNull Identifier getCharacterID() {
		return CustomToad.ID;
	}

	@Override
	public @NotNull Identifier getFormID() {
		return Fire.ID;
	}

	@Override
	public AppearanceModel createModel(ModelPart root) {
		return new CustomizableToadAppearanceModel(root);
	}

	@Override
	public void accumulateCustomFeatureRenderers(ImmutableList.Builder<FeatureRenderer<AbstractClientPlayerEntity, AppearanceModel>> builder, FeatureRendererContext<AbstractClientPlayerEntity, AppearanceModel> featureRendererContext, EntityRendererFactory.Context ctx) {
		builder.addAll(CustomizableTextureLayerFeature.makeCustomToadFeatures(featureRendererContext, "super", CustomizableTextureLayerFeature.SpotsMode.HARDCODED));
	}
}
