package com.fqf.charaformact.mixin.client.features;

import com.fqf.charaformact.appearance.FeatureRendererWithContext;
import com.fqf.charaformact.util.TransformationContext;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FeatureRenderer.class)
public class FeatureRendererContextMixin implements FeatureRendererWithContext {
	@Unique
	private TransformationContext context = FeatureRendererWithContext.getAssumedContext(this.getClass());

	@Override
	public @NotNull TransformationContext cfa$getContext() {
		return this.context;
	}

	@Override
	public void cfa$setContext(@NotNull TransformationContext newContext) {
		this.context = newContext;
	}
}
