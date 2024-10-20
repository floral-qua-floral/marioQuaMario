package com.floralquafloral.mixin;

import com.floralquafloral.MarioQuaMarioClient;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> {

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/LivingEntityRenderer;getLyingAngle(Lnet/minecraft/entity/LivingEntity;)F"), method = "setupTransforms", cancellable = true)
	private void squashedDeathAnimation(T entity, MatrixStack matrices, float animationProgress, float bodyYaw, float tickDelta, float scale, CallbackInfo ci) {
		if(MarioQuaMarioClient.SQUASHED_ENTITIES.contains(entity)) {
			matrices.scale(1.4F, 0.25F, 1.4F);
			ci.cancel();
		}
	}
}
