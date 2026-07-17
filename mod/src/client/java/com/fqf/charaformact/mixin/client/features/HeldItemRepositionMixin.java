package com.fqf.charaformact.mixin.client.features;

import com.fqf.charaformact.appearance.ParsedClientAppearance;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithArms;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemFeatureRenderer.class)
public abstract class HeldItemRepositionMixin<T extends LivingEntity, M extends EntityModel<T> & ModelWithArms> extends FeatureRenderer<T, M> {
	public HeldItemRepositionMixin(FeatureRendererContext<T, M> context) {
		super(context);
	}

	@Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;push()V", shift = At.Shift.AFTER))
	private void changeMatrixStack(
			MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light,
			T entity,
			float limbAngle, float limbDistance,
			float tickDelta, float animationProgress,
			float headYaw, float headPitch,
			CallbackInfo ci
	) {
		if(entity.isClimbing()) {
			matrices.scale(1, 2, 1);
		}
	}

	@WrapOperation(method = "renderItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"))
	private void alternateTranslate(
			MatrixStack instance, float x, float y, float z, Operation<Void> original,
			@Local(argsOnly = true) LivingEntity entity, @Local(argsOnly = true) ItemStack itemStack
	) {
		if(entity instanceof AbstractClientPlayerEntity player) {
			ParsedClientAppearance model = player.cfa$getAppearanceData().getAppearance();
			if(model != null) {
				float factor = Math.signum(x);
				if(itemStack.getItem() instanceof ShieldItem)
					original.call(instance,
							factor * model.HELD_SHIELD_X_TRANSLATION,
							model.HELD_SHIELD_Y_TRANSLATION,
							model.HELD_SHIELD_Z_TRANSLATION
					);
				else
					original.call(instance,
							factor * model.HELD_ITEM_X_TRANSLATION,
							model.HELD_ITEM_Y_TRANSLATION,
							model.HELD_ITEM_Z_TRANSLATION
					);
				return;
			}
		}
		original.call(instance, x, y, z);
	}
}
