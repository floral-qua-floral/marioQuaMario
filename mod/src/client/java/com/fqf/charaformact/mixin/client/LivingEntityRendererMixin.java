package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.appearance.ClientAppearanceCollector;
import com.fqf.charaformact.appearance.FeatureRendererWithContext;
import com.fqf.charaformact.appearance.FeatureRendererWithMutableRenderer;
import com.fqf.charaformact.appearance.ParsedClientAppearance;
import com.fqf.charaformact.util.ModelPartMover;
import com.fqf.charaformact.util.TransformationContext;
import com.fqf.charaformact_api.appearance.AppearanceModel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.*;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
	@Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;features:Ljava/util/List;"))
	private void adjustBodyPartsForFeatures(
			T livingEntity, float yaw,
			float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
			CallbackInfo ci, @Share("mutatePosture") LocalBooleanRef applyRef
			) {
		if(livingEntity instanceof AbstractClientPlayerEntity player) {
			ParsedClientAppearance parsedModel = player.cfa$getAppearanceData().getAppearance();
			if(parsedModel != null) {
				applyRef.set(true);
				AppearanceModel entityModel = parsedModel.getModel();
				ModelPartMover.instance = new ModelPartMover(parsedModel, entityModel);
			}
		}
	}

	@Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
	private void killModelPartMover(
			T livingEntity, float yaw,
			float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
			CallbackInfo ci, @Share("mutatePosture") LocalBooleanRef applyRef
	) {
		if(applyRef.get()) ModelPartMover.instance = null;
	}

	@WrapOperation(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/FeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/Entity;FFFFFF)V"))
	private <Z extends Entity> void renderFeature(
			FeatureRenderer<Z, ?> instance, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
			int light, Z entity,
			float limbAngle, float limbDistance, float tickDelta, float animationProgress,
			float headYaw, float headPitch,
			Operation<Void> original, @Share("mutatePosture") LocalBooleanRef applyRef, @Share(namespace = "cfa", value = "mover") LocalRef<ModelPartMover> moverRef
	) {
		if(entity instanceof AbstractClientPlayerEntity player) {
			((FeatureRendererWithMutableRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>>) instance).cfa$replaceMutableContext(player.cfa$getAppearanceData().getRenderer());
		}
		if(applyRef.get()) {
			TransformationContext context = ((FeatureRendererWithContext) instance).cfa$getContext();
			ModelPartMover.instance.setTo(context);
		}
		original.call(instance, matrices, vertexConsumers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
	}

	@Inject(method = "addFeature", at = @At("RETURN"))
	private void maybeCaptureFeatures(FeatureRenderer<T, M> feature, CallbackInfoReturnable<Boolean> cir) {
		if(this.isCapturingFeatures()) {
			CharaFormAct.LOGGER.info("GOTCHA! Captured a feature: {}", feature);
			ClientAppearanceCollector.INSTANCE.captureFeature(feature);
		}
	}

	@Unique
	protected boolean isCapturingFeatures() {
		return false;
	}
}
