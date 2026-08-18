package com.fqf.charaformact.appearance;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.cfadata.CfaAppearanceData;
import com.fqf.charaformact.cfadata.equipment.UpdateEquipmentRenderingCallback;
import com.fqf.charaformact.util.LivingEntityRendererMixinInterface;
import com.fqf.charaformact.util.ModelPartMover;
import com.fqf.charaformact_api.appearance.equipment.EquipmentFeatureCategory;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;

import java.util.Comparator;
import java.util.function.Function;

public class AppearanceRenderer extends PlayerEntityRenderer implements LivingEntityRendererMixinInterface<AbstractClientPlayerEntity> {
	public final Function<AbstractClientPlayerEntity, Identifier> TEXTURE_FUNCTION;
	private boolean featuresNeedSorting = true;

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
		if(!this.featuresNeedSorting) {
			this.featuresNeedSorting = true;
			CharaFormAct.LOGGER.warn("Added a new Feature Renderer after the Appearance has already started rendering?" +
					" This is weird, why are you doing this?");
		}
	}

	@Override
	protected float getShadowRadius(AbstractClientPlayerEntity livingEntity) {
		return super.getShadowRadius(livingEntity) * livingEntity.cfa$getCfaData().getHorizontalScale();
	}

	public void sortFeatures() {
		this.features.sort(Comparator.comparingInt(feature ->
				((RecategorizableFeatureRenderer) feature).cfa$getMutableCategory().ordinal()));
		if(CharaFormAct.CONFIG.gameLaunchLogging()) CharaFormAct.LOGGER.info("Sorted features for {}!", this);
	}

	@Override
	public void cfa$prepareModelPartMover(AbstractClientPlayerEntity livingEntity) {
		if(this.featuresNeedSorting) {
			this.featuresNeedSorting = false;
			this.sortFeatures();
		}

		CfaAppearanceData<?> appearanceData = livingEntity.cfa$getAppearanceData();
		ParsedClientAppearance parsedModel = appearanceData.getAppearance();
		// ^ Never null; if it were, this rendering wouldn't be done by an AppearanceRenderer in the first place

		ModelPartMover.instance = new ModelPartMover(parsedModel);

		// Update covering data once per tick. We do this here so that Cosmetic Armor mods will take effect.
		if(appearanceData.needsCoveringUpdate) {
			appearanceData.needsCoveringUpdate = false;
			UpdateEquipmentRenderingCallback.EVENT.invoker().onUpdateEquipment(livingEntity);
		}
	}

	@Override
	public void cfa$prepareToRenderFeature(FeatureRenderer<? extends Entity, ?> instance) {
		//noinspection unchecked
		((FeatureRendererWithMutableRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>) instance).cfa$replaceMutableContext(this);
		EquipmentFeatureCategory context = ((RecategorizableFeatureRenderer) instance).cfa$getMutableCategory();
		ModelPartMover.instance.setTo(context);
	}

	@Override
	public void cfa$killModelPartMover() {
		ModelPartMover.instance = null;
	}
}
