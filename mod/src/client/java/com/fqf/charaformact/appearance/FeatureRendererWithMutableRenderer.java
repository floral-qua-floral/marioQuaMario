package com.fqf.charaformact.appearance;

import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.entity.Entity;

public interface FeatureRendererWithMutableRenderer<T extends Entity, M extends EntityModel<T>> {
	default void cfa$replaceMutableContext(FeatureRendererContext<T, M> newContext) {
		throw new IllegalStateException("Needs to be implemented!");
	}
}
