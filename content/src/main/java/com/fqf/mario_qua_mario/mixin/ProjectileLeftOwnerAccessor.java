package com.fqf.mario_qua_mario.mixin;

import net.minecraft.entity.projectile.ProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ProjectileEntity.class)
public interface ProjectileLeftOwnerAccessor {
	@Accessor
	boolean getLeftOwner();
}
