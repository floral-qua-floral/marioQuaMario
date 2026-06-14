package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.charaformact_api.util.Easing;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.Squashable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
	@Unique private static final float IN_OUT_RATIO = 6;

	@Inject(method = "setupTransforms", at = @At("HEAD"))
	private void squashOnFlatteningDamage(T entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta, float scale, CallbackInfo ci) {
		float trueSquashProgress = ((Squashable) entity).cfa$getSquashProgress(tickDelta);
		float squashProgress;
		if(trueSquashProgress != 0) {
			if(entity.isDead()) {
				squashProgress = Math.min(trueSquashProgress * 4, 1);
				float horizontalScale = Easing.LINEAR.ease(squashProgress, 1, 1.4F);
				float verticalScale = Easing.LINEAR.ease(squashProgress, 1, 0.225F);
				matrices.scale(horizontalScale, verticalScale, horizontalScale);
			}
			else if(MarioQuaMario.CONFIG.doSquashDamageAnimation()) {
				squashProgress = trueSquashProgress * IN_OUT_RATIO;
				float yScale;
				if (squashProgress < 1) yScale = Easing.LINEAR.ease(squashProgress, 1, 0.5F);
				else yScale = Easing.ELASTIC_OUT.ease((squashProgress - 1) / (IN_OUT_RATIO - 1), 0.5F, 1);
				matrices.scale(1, yScale, 1);
			}
		}
	}

	@WrapOperation(method = "setupTransforms", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getLyingAngle(Lnet/minecraft/entity/LivingEntity;)F"))
	private float squashDuringDeathAnimation(LivingEntityRenderer<T, M> instance, T entity, Operation<Float> original, @Local(argsOnly = true) MatrixStack matrices) {
		if(((Squashable) entity).cfa$isSquashed()) return 0;
		return original.call(instance, entity);
	}
}
