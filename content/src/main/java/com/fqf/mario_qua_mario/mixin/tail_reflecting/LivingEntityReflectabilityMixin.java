package com.fqf.mario_qua_mario.mixin.tail_reflecting;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LivingEntity.class)
public abstract class LivingEntityReflectabilityMixin extends EntityReflectabilityMixin {
	@Shadow public abstract boolean isUsingRiptide();

	@Override
	public boolean cfa$canReflect() {
		return super.cfa$canReflect() || this.isUsingRiptide();
	}
}
