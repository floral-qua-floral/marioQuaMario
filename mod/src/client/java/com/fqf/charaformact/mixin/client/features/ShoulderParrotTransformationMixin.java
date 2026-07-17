package com.fqf.charaformact.mixin.client.features;

import com.fqf.charaformact.appearance.ParsedClientAppearance;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.ShoulderParrotFeatureRenderer;
import net.minecraft.client.render.entity.model.ParrotEntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ShoulderParrotFeatureRenderer.class)
public abstract class ShoulderParrotTransformationMixin<T extends PlayerEntity> extends FeatureRenderer<T, PlayerEntityModel<T>> {
	public ShoulderParrotTransformationMixin(FeatureRendererContext<T, PlayerEntityModel<T>> context) {
		super(context);
	}

	// doing a mixin on a lambda is scary :(
	@WrapOperation(method = "method_17958", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V"))
	private void translateParrotDifferently(
			MatrixStack instance, float x, float y, float z,
			Operation<Void> original, @Local(argsOnly = true) T player
	) {
		if(player instanceof AbstractClientPlayerEntity abstractPlayer) {
			ParsedClientAppearance model = abstractPlayer.cfa$getAppearanceData().getAppearance();
			if(model != null) {
				this.getContextModel().body.rotate(instance);
				original.call(instance,
						Math.signum(x) * model.SHOULDER_PARROT_X_TRANSLATION,
						model.SHOULDER_PARROT_Y_TRANSLATION,
						model.SHOULDER_PARROT_Z_TRANSLATION
				);
				return;
			}
		}
		original.call(instance, x, y, z);
	}

	@WrapOperation(method = "method_17958", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/model/ParrotEntityModel;poseOnShoulder(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFFI)V"))
	private void poseModelWithHeadCounterRotation(
			ParrotEntityModel instance,
			MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay,
			float limbAngle, float limbDistance, float headYaw, float headPitch,
			int danceAngle,
			Operation<Void> original, @Local(argsOnly = true) T player
	) {
		if(player instanceof AbstractClientPlayerEntity abstractPlayer && abstractPlayer.cfa$getAppearanceData().hasAppearance()) {
			ModelPart body = this.getContextModel().body;
			headYaw -= body.yaw;
			headPitch -= body.pitch;
		}
		original.call(instance, matrices, vertexConsumer, light, overlay, limbAngle, limbDistance, headYaw, headPitch, danceAngle);
	}
}
