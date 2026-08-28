package com.fqf.mario_qua_mario.mixin.tail_reflecting;

import net.minecraft.entity.mob.VexEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(VexEntity.class)
public abstract class VexReflectabilityMixin extends LivingEntityReflectabilityMixin {
	@Shadow public abstract boolean isCharging();

	@Override public boolean cfa$canReflect() {
		return super.cfa$canReflect() || this.isCharging();
	}
}
