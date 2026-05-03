package com.fqf.charapoweract_api.mixin.collision_attackables;

import com.fqf.charapoweract_api.interfaces.CollisionAttackResult;
import com.fqf.charapoweract_api.interfaces.CollisionAttackable;
import com.fqf.charapoweract_api.cpadata.ICPAAuthoritativeData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AbstractHorseEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractHorseEntity.class)
public class AbstractHorseEntityMixin implements CollisionAttackable {
	@Override
	public @NotNull CollisionAttackResult cpa$processCollisionAttack(ICPAAuthoritativeData data, boolean attemptMount, float damageAmount, DamageSource damageSource) {
		if(data.getPlayer().startRiding((Entity) (Object) this, false))
			return CollisionAttackResult.MOUNT;
		else
			return CollisionAttackable.super.cpa$processCollisionAttack(data, attemptMount, damageAmount, damageSource);
	}
}
