package com.fqf.charaformact_api.mixin.collision_attackables;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.interfaces.CollisionAttackResult;
import com.fqf.charaformact_api.interfaces.CollisionAttackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractHorseEntity.class)
public class AbstractHorseEntityMixin implements CollisionAttackable {
	@Override
	public @NotNull CollisionAttackResult cfa$processCollisionAttack(CfaAuthoritativeData data, boolean attemptMount, float damageAmount, DamageSource damageSource) {
		if(data.getPlayer().startRiding((Entity) (Object) this, false))
			return CollisionAttackResult.MOUNT;
		else
			return CollisionAttackable.super.cfa$processCollisionAttack(data, attemptMount, damageAmount, damageSource);
	}
}
