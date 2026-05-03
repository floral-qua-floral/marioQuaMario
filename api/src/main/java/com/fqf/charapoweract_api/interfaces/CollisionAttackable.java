package com.fqf.charapoweract_api.interfaces;

import com.fqf.charapoweract_api.cpadata.ICPAAuthoritativeData;
import com.fqf.charapoweract_api.util.CPATags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.NotNull;

public interface CollisionAttackable {
	default @NotNull CollisionAttackResult cpa$processCollisionAttack(
		ICPAAuthoritativeData data,
		boolean attemptMount,
		float damageAmount, DamageSource damageSource
	) {
		if(this instanceof Entity thisEntity) {
			if(thisEntity.getType().isIn(CPATags.NOT_HIT_BY_COLLISION_ATTACKS)) return CollisionAttackResult.FAIL;
			if(thisEntity.getType().isIn(CPATags.HARMS_COLLISION_ATTACKERS)) return CollisionAttackResult.PAINFUL;

			if(this instanceof Saddleable thisSaddleable) {
				if(thisSaddleable.isSaddled() && data.getPlayer().startRiding(thisEntity, false))
					return CollisionAttackResult.MOUNT;
			}

			boolean damaged = thisEntity.damage(damageSource, damageAmount);
			if(damaged) {
				data.getPlayer().onAttacking(thisEntity);
				return CollisionAttackResult.NORMAL;
			}
			else return CollisionAttackResult.RESISTED;
		}
		else return CollisionAttackResult.FAIL;
	}
}
