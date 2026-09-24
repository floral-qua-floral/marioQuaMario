package com.fqf.mario_qua_mario.mixin.client.freezing;

import com.fqf.mario_qua_mario.freezing.EncasementRenderUtil;
import com.fqf.mario_qua_mario.freezing.IceFlowerFreezable;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public abstract class EncasedEntitiesRenderIceMixin {
	@WrapMethod(method = "render")
	private <E extends Entity> void noPoseLerp(
			E entity, double x, double y, double z, float yaw,
			float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers,
			int light, Operation<Void> original
	) {
		IceFlowerFreezable freezable = (IceFlowerFreezable) entity;
		boolean encased = freezable.mqm$isEncased();
		if(encased && freezable.mqm$isShaking()) {
			double deviation = 0.05;
			x += entity.getRandom().nextTriangular(0, deviation);
			if(!entity.isOnGround()) y += entity.getRandom().nextTriangular(0, deviation);
			z += entity.getRandom().nextTriangular(0, deviation);
		}
		//noinspection MixinExtrasOperationParameters
		original.call(entity, x, y, z, yaw, encased ? 0 : tickDelta, matrices, vertexConsumers, light);
	}

	@Inject(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/entity/EntityRenderer;render(Lnet/minecraft/entity/Entity;FFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V",
					shift = At.Shift.AFTER
			)
	)
	private <T extends Entity> void renderIceBlock(
			T entity, double x, double y, double z, float yaw, float tickDelta,
			MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci
	) {
		if(((IceFlowerFreezable) entity).mqm$isEncased())
			EncasementRenderUtil.renderEncasingIce(entity, matrices, vertexConsumers, light);
	}
}
