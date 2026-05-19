package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.models.FeatureRendererWithContext;
import com.fqf.charaformact.util.TransformationContext;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(FeatureRenderer.class)
public class FeatureRendererContextMixin implements FeatureRendererWithContext {
	@Unique
	private final TransformationContext CONTEXT = FeatureRendererWithContext.getAssumedContext(this.getClass());

	@Override
	public @Nullable TransformationContext cfa$getContext() {
		return this.CONTEXT;
	}
}
