package com.fqf.charaformact.mixin.client.features;

import com.fqf.charaformact.appearance.FeatureRendererWithContext;
import com.fqf.charaformact.util.TransformationContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.feature.CapeFeatureRenderer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(CapeFeatureRenderer.class)
public class CapeFeatureRendererMixin implements FeatureRendererWithContext {
	@Override
	public @NotNull TransformationContext cfa$getContext() {
		// Cape should render as back equipment.
		return TransformationContext.SPECIAL;
	}

	@WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/network/AbstractClientPlayerEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;isInSneakingPose()Z"))
	private boolean noSneakOffsetAllowedGrr(AbstractClientPlayerEntity instance, Operation<Boolean> original) {
		if(instance.cfa$getCfaData().isEnabled()) return false;
		return original.call(instance);
	}
}
