package com.fqf.charapoweract.mixin.client;

import com.fqf.charapoweract.bapping.BlockBappingClientUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;getPositionOffset(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/util/math/Vec3d;"))
	private <E extends Entity> Vec3d modifyEntityOffset(EntityRenderer<? super E> instance, E entity, float tickDelta, Operation<Vec3d> original) {
		return original.call(instance, entity, tickDelta).add(BlockBappingClientUtil.calculateDubiousOffsetUnder(entity, tickDelta));
	}
}
