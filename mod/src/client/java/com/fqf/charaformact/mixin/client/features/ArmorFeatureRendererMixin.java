package com.fqf.charaformact.mixin.client.features;

import com.fqf.charaformact.appearance.FeatureRendererWithContext;
import com.fqf.charaformact.util.ModelPartMover;
import com.fqf.charaformact.util.TransformationContext;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin<T extends LivingEntity, A extends BipedEntityModel<T>> implements FeatureRendererWithContext {
//	@Inject(method = "renderArmorParts", at = @At("HEAD"))
//	private void test(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, A model, int i, Identifier identifier, CallbackInfo ci) {
//		matrices.translate(0, -0.5, 0);
//		CharaFormAct.LOGGER.info("Model: {}", model);
//	}

	@Override
	public @Nullable TransformationContext cfa$getContext() {
		return null;
	}

	@WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V"))
	private void renderArmor(
			ArmorFeatureRenderer<?, ?, ?> instance, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
			T entity, EquipmentSlot armorSlot, int light, A model,
			Operation<Void> original
	) {
		if(ModelPartMover.instance != null) {
			TransformationContext context = switch(armorSlot) {
				case LEGS -> TransformationContext.ARMOR_INNER;
				case FEET, CHEST, HEAD -> TransformationContext.ARMOR_OUTER;
				default -> null;
			};
			if(context != null) ModelPartMover.instance.setTo(context);
		}
		original.call(instance, matrices, vertexConsumers, entity, armorSlot, light, model);
	}
}
