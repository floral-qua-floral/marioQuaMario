package com.fqf.charaformact_api.mixin.client.equipment;

import com.fqf.charaformact_api.appearance.equipment.EquipmentFeatureCategory;
import com.fqf.charaformact_api.appearance.equipment.EquipmentCategoryProvider;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

/**
 * The automatic feature categorization would have caught and correctly identified these features as SPECIAL even
 * without them implementing EquipmentCategoryProvider. The purpose of this mixin is simply to make this categorization
 * explicit, and to serve as another example of how to categorize Feature Renderers.
 */
@Mixin(value = {ElytraFeatureRenderer.class, CapeFeatureRenderer.class})
public class SpecialEquipmentCategorizationMixin implements EquipmentCategoryProvider {
	@Override
	public @NotNull EquipmentFeatureCategory cfa$defineEquipmentCategory() {
		return EquipmentFeatureCategory.SPECIAL;
	}
}
