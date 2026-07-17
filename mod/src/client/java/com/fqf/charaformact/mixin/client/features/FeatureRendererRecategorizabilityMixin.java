package com.fqf.charaformact.mixin.client.features;

import com.fqf.charaformact.appearance.RecategorizableFeatureRenderer;
import com.fqf.charaformact_api.appearance.equipment.EquipmentFeatureCategory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FeatureRenderer.class)
public class FeatureRendererRecategorizabilityMixin implements RecategorizableFeatureRenderer {
	@Unique private EquipmentFeatureCategory context = RecategorizableFeatureRenderer.getInitialCategory(this);

	@Override
	public @NotNull EquipmentFeatureCategory cfa$getMutableCategory() {
		return this.context;
	}

	@Override
	public void cfa$recategorize(@NotNull EquipmentFeatureCategory newCategory) {
		this.context = newCategory;
	}
}
