package com.fqf.charaformact_api.mixin.collision_attackables;

import com.fqf.charaformact_api.interfaces.CollisionAttackResult;
import com.fqf.charaformact_api.interfaces.CollisionAttackable;
import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractMinecartEntity.class)
public abstract class AbstractMinecartEntityMixin implements CollisionAttackable {
	@Shadow public abstract AbstractMinecartEntity.Type getMinecartType();

	@Override
	public @NotNull CollisionAttackResult cfa$processCollisionAttack(CfaAuthoritativeData data, boolean attemptMount, float damageAmount, DamageSource damageSource) {
		if(this.getMinecartType() == AbstractMinecartEntity.Type.RIDEABLE) {
			if(data.getPlayer().startRiding((Entity) (Object) this, false))
				return CollisionAttackResult.MOUNT;
		}
		return CollisionAttackable.super.cfa$processCollisionAttack(data, attemptMount, damageAmount, damageSource);
	}
}
