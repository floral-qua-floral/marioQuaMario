package com.fqf.mario_qua_mario.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class AbstractMarioProjectileEntity extends ProjectileEntity {
	public AbstractMarioProjectileEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	public abstract boolean hitEntity(Entity target, Entity source, Entity attacker, Entity soundOrigin);
	public abstract boolean attemptBlockInteraction(BlockPos target, World world);
}
