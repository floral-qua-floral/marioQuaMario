package com.fqf.mario_qua_mario.mixin.client.freezing;

import com.fqf.mario_qua_mario.entity.custom.IceEncasementEntity;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityRenderDispatcher.class)
public abstract class DoNotDirectlyRenderEncasedEntitiesMixin {
	@WrapMethod(method = "shouldRender")
	private <E extends Entity> boolean preventRenderingFrozenEntitiesNaturally(
			E entity, Frustum frustum,
			double x, double y, double z,
			Operation<Boolean> original
	) {
		return ((IceEncasementEntity.EncasementOwner) entity).mqm$getEncasement() == null
				&& original.call(entity, frustum, x, y, z);
	}
}
