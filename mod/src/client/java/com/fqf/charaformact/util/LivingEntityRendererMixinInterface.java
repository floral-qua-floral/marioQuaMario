package com.fqf.charaformact.util;

import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

public interface LivingEntityRendererMixinInterface {
	default void cfa$prepareModelPartMover(LivingEntity livingEntity) {

	}

	default void cfa$prepareToRenderFeature(FeatureRenderer<? extends Entity, ?> instance) {

	}

	default void cfa$killModelPartMover() {

	}
}
