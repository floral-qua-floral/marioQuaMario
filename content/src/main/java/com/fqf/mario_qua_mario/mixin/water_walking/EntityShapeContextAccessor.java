package com.fqf.mario_qua_mario.mixin.water_walking;

import net.minecraft.block.EntityShapeContext;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EntityShapeContext.class)
public interface EntityShapeContextAccessor {
	@Accessor("entity")
	Entity mqm$getEntity();
}
