package com.fqf.charaformact.appearance;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class AppearanceRenderer extends PlayerEntityRenderer {
	public final Function<AbstractClientPlayerEntity, Identifier> TEXTURE_FUNCTION;

	public AppearanceRenderer(EntityRendererFactory.Context ctx, ParsedClientAppearance appearance) {
		super(ctx, false);
		this.TEXTURE_FUNCTION = appearance.TEXTURE_FUNCTION;
		for(var customFeature : appearance.makeCustomFeatures(this, ctx)) {
			this.addFeature(customFeature);
		}
	}

	@Override
	public Identifier getTexture(AbstractClientPlayerEntity abstractClientPlayerEntity) {
		return this.TEXTURE_FUNCTION.apply(abstractClientPlayerEntity);
	}

	public void addCapturedFeature(FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> feature) {
		this.addFeature(feature);
	}

	@Override
	protected float getShadowRadius(AbstractClientPlayerEntity livingEntity) {
		return super.getShadowRadius(livingEntity) * livingEntity.cfa$getCfaData().getHorizontalScale();
	}
}
