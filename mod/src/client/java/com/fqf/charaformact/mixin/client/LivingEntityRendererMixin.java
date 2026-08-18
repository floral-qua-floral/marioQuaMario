package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.CharaFormAct;
import com.fqf.charaformact.appearance.ClientAppearanceCollector;
import com.fqf.charaformact.util.LivingEntityRendererMixinInterface;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> implements LivingEntityRendererMixinInterface<T> {
	@Inject(
			method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;features:Ljava/util/List;",
					opcode = Opcodes.GETFIELD
			)
	)
	private void adjustBodyPartsForFeatures(
			T livingEntity, float yaw,
			float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
			CallbackInfo ci
	) {
		this.cfa$prepareModelPartMover(livingEntity);
	}

	@WrapOperation(
			method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/entity/feature/FeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/Entity;FFFFFF)V"
			)
	)
	private <Z extends Entity> void renderFeature(
			FeatureRenderer<Z, ?> instance, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
			int light, Z entity,
			float limbAngle, float limbDistance, float tickDelta, float animationProgress,
			float headYaw, float headPitch,
			Operation<Void> original
	) {
		this.cfa$prepareToRenderFeature(instance);
		original.call(instance, matrices, vertexConsumers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch);
	}

	@Inject(
			method = "render(Lnet/minecraft/entity/LivingEntity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
			at = @At("TAIL")
	)
	private void killModelPartMover(
			T livingEntity, float yaw,
			float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
			CallbackInfo ci
	) {
		this.cfa$killModelPartMover();
	}

	@Inject(method = "addFeature", at = @At("RETURN"))
	private void maybeCaptureFeatures(FeatureRenderer<T, M> feature, CallbackInfoReturnable<Boolean> cir) {
		if(this.isCapturingFeatures()) {
			if(CharaFormAct.CONFIG.gameLaunchLogging())
				CharaFormAct.LOGGER.info("GOTCHA! Captured a feature: {}. Distributing...", feature);
			ClientAppearanceCollector.INSTANCE.captureFeature(feature);
		}
	}

	@Unique protected boolean isCapturingFeatures() {
		return false;
	}
}
