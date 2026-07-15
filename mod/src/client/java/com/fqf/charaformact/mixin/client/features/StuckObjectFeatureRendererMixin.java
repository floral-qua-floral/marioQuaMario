package com.fqf.charaformact.mixin.client.features;

import com.fqf.charaformact.appearance.FeatureRendererWithContext;
import com.fqf.charaformact.util.TransformationContext;
import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.StuckObjectsFeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(StuckObjectsFeatureRenderer.class)
public abstract class StuckObjectFeatureRendererMixin<T extends LivingEntity, M extends PlayerEntityModel<T>> extends FeatureRenderer<T, M> implements FeatureRendererWithContext {
	public StuckObjectFeatureRendererMixin(FeatureRendererContext<T, M> context) {
		super(context);
	}

	// Stuck Object features directly attach to cuboids instead of making assumptions about what goes where, so we let
	// them place themselves without messing with them.
	@Override public @NotNull TransformationContext cfa$getContext() {
		return TransformationContext.ORIGINAL;
	}

	@WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;rotate(Lnet/minecraft/client/util/math/MatrixStack;)V"))
	private void useRotatorFromRoot(ModelPart instance, MatrixStack matrices, Operation<Void> original) {
		if(this.getContextModel() instanceof AppearanceModel appearanceModel)
			appearanceModel.rotateFromRoot(instance, matrices);
		else
			original.call(instance, matrices);
	}
}
