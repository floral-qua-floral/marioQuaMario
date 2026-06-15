package com.fqf.mario_qua_mario.mixin.tail_reflecting;

import net.minecraft.entity.projectile.ShulkerBulletEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShulkerBulletEntity.class)
public abstract class ShulkerBulletReflectabilityMixin extends ProjectileReflectabilityMixin {
	// Shulker bullets cannot be reflected. This is both a design choice and a technical one. They're so slow that
	// they'd be a little too easy to reflect, and it also would be kind of mechanically wonky since they can just
	// turn right back around, which would cause the player to effectively Shulker-bullet themself.
	// Also, for some reason reflecting a Shulker Bullet instantly destroys it anyways???

	@Override
	public boolean cfa$canReflect() {
		return false;
	}
}
