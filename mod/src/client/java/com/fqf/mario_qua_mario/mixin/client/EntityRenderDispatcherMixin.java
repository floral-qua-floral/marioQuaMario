package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.bapping.BlockBappingClientUtil;
import com.fqf.mario_qua_mario.bapping.BumpedBlockParticle;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Map;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
	@WrapOperation(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/EntityRenderer;getPositionOffset(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/util/math/Vec3d;"))
	private <E extends Entity> Vec3d modifyEntityOffset(EntityRenderer<? super E> instance, E entity, float tickDelta, Operation<Vec3d> original) {
		Vec3d offset = original.call(instance, entity, tickDelta);
		BumpedBlockParticle particle = BlockBappingClientUtil.getBumpedBlockUnder(entity);
		if(particle != null)
			return particle.applyOffset(offset, tickDelta);
		return offset;
	}
}
