package com.fqf.mario_qua_mario.mixin.tail_reflecting;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(TridentEntity.class)
public abstract class TridentEntityMixin extends PersistentProjectileMixin {
	public TridentEntityMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Shadow private boolean dealtDamage;

	@Override
	public void mqm$dislodge() {
		super.mqm$dislodge();
		this.inGroundTime = 0;
		this.dealtDamage = false;
	}
}
