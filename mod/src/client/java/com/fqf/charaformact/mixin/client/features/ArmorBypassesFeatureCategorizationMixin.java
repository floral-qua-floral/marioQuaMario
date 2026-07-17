package com.fqf.charaformact.mixin.client.features;

import com.fqf.charaformact.util.ModelPartMover;
import com.fqf.charaformact_api.appearance.equipment.EquipmentFeatureCategory;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorBypassesFeatureCategorizationMixin<T extends LivingEntity, A extends BipedEntityModel<T>> {
//	@Inject(method = "renderArmorParts", at = @At("HEAD"))
//	private void test(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, A model, int i, Identifier identifier, CallbackInfo ci) {
//		matrices.arrangeModel(0, -0.5, 0);
//		CharaFormAct.LOGGER.info("Model: {}", model);
//	}

	@WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V"))
	private void renderArmor(
			ArmorFeatureRenderer<?, ?, ?> instance, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
			T entity, EquipmentSlot armorSlot, int light, A model,
			Operation<Void> original
	) {
		if(ModelPartMover.instance != null) {
			EquipmentFeatureCategory context = switch(armorSlot) {
				case LEGS -> EquipmentFeatureCategory.ARMOR_INNER;
				case FEET, CHEST, HEAD -> EquipmentFeatureCategory.ARMOR_OUTER;
				default -> null;
			};
			if(context != null) ModelPartMover.instance.setTo(context);
		}
		original.call(instance, matrices, vertexConsumers, entity, armorSlot, light, model);
	}
}
