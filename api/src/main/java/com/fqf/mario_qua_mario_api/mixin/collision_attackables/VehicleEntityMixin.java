package com.fqf.mario_qua_mario_api.mixin.collision_attackables;

import com.fqf.mario_qua_mario_api.interfaces.CollisionAttackResult;
import com.fqf.mario_qua_mario_api.interfaces.CollisionAttackable;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.vehicle.VehicleEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(VehicleEntity.class)
public class VehicleEntityMixin implements CollisionAttackable {
	@Override
	public @NotNull CollisionAttackResult mqm$processCollisionAttack(IMarioAuthoritativeData marioData, boolean attemptMount, float damageAmount, DamageSource damageSource) {
		if(marioData.getMario().startRiding((Entity) (Object) this, false))
			return CollisionAttackResult.MOUNT;
		else
			return CollisionAttackable.super.mqm$processCollisionAttack(marioData, attemptMount, damageAmount, damageSource);
	}
}
