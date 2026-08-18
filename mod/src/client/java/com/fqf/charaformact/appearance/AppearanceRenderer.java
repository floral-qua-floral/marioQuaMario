package com.fqf.charaformact.appearance;

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
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public class AppearanceRenderer extends PlayerEntityRenderer implements LivingEntityRendererMixinInterface {
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

	@Override
	public void cfa$prepareModelPartMover(LivingEntity livingEntity) {
		AbstractClientPlayerEntity player = (AbstractClientPlayerEntity) livingEntity;

		CfaAppearanceData<?> appearanceData = player.cfa$getAppearanceData();
		ParsedClientAppearance parsedModel = appearanceData.getAppearance();
		// ^ Never null; if it were, this rendering wouldn't be done by an AppearanceRenderer in the first place

		ModelPartMover.instance = new ModelPartMover(parsedModel);

		// Update covering data once per tick. We do this here so that Cosmetic Armor mods will take effect.
		if(appearanceData.needsCoveringUpdate) {
			appearanceData.needsCoveringUpdate = false;
			UpdateEquipmentRenderingCallback.EVENT.invoker().onUpdateEquipment(player);
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
