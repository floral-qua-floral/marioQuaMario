package com.fqf.mario_qua_mario_api.interfaces;

import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.util.MQMTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.damage.DamageSource;
import org.jetbrains.annotations.NotNull;

public interface Stompable {
	default @NotNull StompResult mqm$stomp(
		IMarioAuthoritativeData marioData,
		boolean attemptMount,
		float damageAmount, DamageSource damageSource
	) {
		if(this instanceof Entity thisEntity) {
			if(thisEntity.getType().isIn(MQMTags.UNSTOMPABLE)) return StompResult.FAIL;
			if(thisEntity.getType().isIn(MQMTags.HURTS_TO_STOMP)) return StompResult.PAINFUL;

			if(this instanceof Saddleable thisSaddleable) {
				if(thisSaddleable.isSaddled() && marioData.getMario().startRiding(thisEntity, false))
					return StompResult.MOUNT;
			}

			boolean damaged = thisEntity.damage(damageSource, damageAmount);
			if(damaged) {
				marioData.getMario().onAttacking(thisEntity);
				return StompResult.NORMAL;
			}
			else return StompResult.RESISTED;
		}
		else return StompResult.FAIL;
	}
}
