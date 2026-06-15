package com.fqf.mario_qua_mario.mixin.tail_reflecting;

import com.fqf.mario_qua_mario.util.ReflectableEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ProjectileEntity.class)
public abstract class ProjectileReflectabilityMixin extends EntityReflectabilityMixin implements ReflectableEntity {
	@Override
	public boolean cfa$canReflect() {
		return true;
	}
}
