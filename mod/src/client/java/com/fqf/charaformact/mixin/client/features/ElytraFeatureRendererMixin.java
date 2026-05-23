package com.fqf.charaformact.mixin.client.features;

import com.fqf.charaformact.appearance.FeatureRendererWithContext;
import com.fqf.charaformact.appearance.ParsedClientAppearance;
import com.fqf.charaformact.util.TransformationContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ElytraFeatureRenderer.class)
public abstract class ElytraFeatureRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> implements FeatureRendererWithContext {
	public ElytraFeatureRendererMixin(FeatureRendererContext<T, M> context) {
		super(context);
	}

	@Override
	public @Nullable TransformationContext cfa$getContext() {
		return TransformationContext.SPECIAL;
	}

	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/ElytraEntityModel;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;II)V"))
	private void adjustMatrices(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
		if(livingEntity instanceof AbstractClientPlayerEntity player) {
			ParsedClientAppearance appearance = player.cfa$getAppearanceData().getAppearance();
			if(appearance != null)
				appearance.getModel().body.rotate(matrixStack);
		}
	}
}
