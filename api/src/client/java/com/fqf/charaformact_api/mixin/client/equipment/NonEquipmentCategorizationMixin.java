package com.fqf.charaformact_api.mixin.client.equipment;

import com.fqf.charaformact_api.appearance.equipment.EquipmentFeatureCategory;
import com.fqf.charaformact_api.appearance.equipment.EquipmentCategoryProvider;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.feature.ShoulderParrotFeatureRenderer;
import net.minecraft.client.render.entity.feature.StuckObjectsFeatureRenderer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {HeldItemFeatureRenderer.class, ShoulderParrotFeatureRenderer.class, StuckObjectsFeatureRenderer.class})
public class NonEquipmentCategorizationMixin implements EquipmentCategoryProvider {
	@Override
	public @NotNull EquipmentFeatureCategory cfa$defineEquipmentCategory() {
		return EquipmentFeatureCategory.NOT_EQUIPMENT;
	}
}
