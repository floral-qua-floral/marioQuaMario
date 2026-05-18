package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.models.ParsedCharacterFormModel;
import com.fqf.charaformact.util.ModelPartMover;
import com.fqf.charaformact.util.TransformationContext;
import com.fqf.charaformact_api.model.CharacterFormEntityModel;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity> {
	@Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;features:Ljava/util/List;"))
	private void adjustBodyPartsForFeatures(
			T livingEntity, float yaw,
			float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
			CallbackInfo ci, @Share("apply") LocalBooleanRef applyRef
			) {
		if(livingEntity instanceof AbstractClientPlayerEntity player) {
			ParsedCharacterFormModel parsedModel = player.cfa$getModelData().getModel();
			if(parsedModel != null) {
				applyRef.set(true);
				CharacterFormEntityModel entityModel = parsedModel.getModel();
				ModelPartMover.instance = new ModelPartMover(parsedModel, entityModel);
			}
		}
	}

	@Inject(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At("TAIL"))
	private void killModelPartMover(
			T livingEntity, float yaw,
			float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
			CallbackInfo ci, @Share("apply") LocalBooleanRef applyRef
	) {
		if(applyRef.get()) ModelPartMover.instance = null;
	}

	@WrapOperation(method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/FeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/Entity;FFFFFF)V"))
	private <Z extends Entity> void renderFeature(
			FeatureRenderer<Z, ?> instance, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
			int light, Z entity,
			float limbAngle, float limbDistance, float tickDelta, float animationProgress,
			float headYaw, float headPitch,
			Operation<Void> original, @Share("apply") LocalBooleanRef applyRef, @Share(namespace = "cfa", value = "mover") LocalRef<ModelPartMover> moverRef
	) {
		if(applyRef.get()) {
			TransformationContext context = getTransformationContext(instance);
			if(context != null) ModelPartMover.instance.setTo(context);
			// Decide which transformation context to use
//			TransformationContext context;
//			if(featureIsArmor(instance)) context = ARMO
		}
		original.call(instance, matrices, vertexConsumers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
	}

	@Unique
	private static TransformationContext getTransformationContext(FeatureRenderer<?, ?> feature) {
		if(feature instanceof HeldItemFeatureRenderer<?, ?>) return TransformationContext.ORIGINAL;
		if(featureIsArmor(feature)) return null; // If it's armor, we want to handle it piece by piece!
		if(featureIsSpecial(feature)) return TransformationContext.SPECIAL;
		return TransformationContext.UNKNOWN;
	}

	@Unique
	private static boolean featureIsArmor(FeatureRenderer<?, ?> feature) {
		if(feature instanceof ArmorFeatureRenderer<?,?,?>) return true;
		return false;
	}

	@Unique
	private static boolean featureIsSpecial(FeatureRenderer<?, ?> feature) {
		if(feature instanceof ElytraFeatureRenderer<?, ?>) return true;
		return false;
	}
}
