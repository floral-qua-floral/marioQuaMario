package com.fqf.charapoweract_api.interfaces;

import com.fqf.charapoweract_api.mariodata.IMarioAuthoritativeData;
import com.fqf.charapoweract_api.util.MQMTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.NotNull;

public interface CollisionAttackable {
	default @NotNull CollisionAttackResult mqm$processCollisionAttack(
		IMarioAuthoritativeData marioData,
		boolean attemptMount,
		float damageAmount, DamageSource damageSource
	) {
		if(this instanceof Entity thisEntity) {
			if(thisEntity.getType().isIn(MQMTags.NOT_HIT_BY_COLLISION_ATTACKS)) return CollisionAttackResult.FAIL;
			if(thisEntity.getType().isIn(MQMTags.HARMS_COLLISION_ATTACKERS)) return CollisionAttackResult.PAINFUL;

			if(this instanceof Saddleable thisSaddleable) {
				if(thisSaddleable.isSaddled() && marioData.getMario().startRiding(thisEntity, false))
					return CollisionAttackResult.MOUNT;
			}

			boolean damaged = thisEntity.damage(damageSource, damageAmount);
			if(damaged) {
				marioData.getMario().onAttacking(thisEntity);
				return CollisionAttackResult.NORMAL;
			}
			else return CollisionAttackResult.RESISTED;
		}
		else return CollisionAttackResult.FAIL;
	}
}
