package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.util.Squashable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {
	@Shadow protected abstract float getLyingAngle(T entity);

	@WrapOperation(method = "setupTransforms", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getLyingAngle(Lnet/minecraft/entity/LivingEntity;)F"))
	private float squashDuringDeathAnimation(LivingEntityRenderer<T, M> instance, T entity, Operation<Float> original, @Local(argsOnly = true) MatrixStack matrices) {
		if(((Squashable) entity).mqm$isSquashed()) {
			matrices.scale(1.4F, 0.225F, 1.4F);
			return 0;
		}
		return this.getLyingAngle(entity); // can't call original 'cause i'm too dumb to figure it out!!! oh well!!!!!!!
	}
}
