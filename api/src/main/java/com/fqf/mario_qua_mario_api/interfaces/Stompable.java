package com.fqf.mario_qua_mario_api.interfaces;

import com.fqf.mario_qua_mario_api.MarioQuaMarioAPI;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public interface Stompable {
	TagKey<EntityType<?>> UNSTOMPABLE_ENTITIES =
			TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("mario_qua_mario:unstompable"));
	TagKey<EntityType<?>> HURTS_TO_STOMP_ENTITIES =
			TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("mario_qua_mario:hurts_to_stomp"));

	default @NotNull StompResult mqm$stomp(
		IMarioAuthoritativeData marioData,
		boolean attemptMount,
		float damageAmount, DamageSource damageSource
	) {
		if(this instanceof Entity thisEntity) {
			if(thisEntity.getType().isIn(UNSTOMPABLE_ENTITIES)) return StompResult.FAIL;
			if(thisEntity.getType().isIn(HURTS_TO_STOMP_ENTITIES)) {
				MarioQuaMarioAPI.LOGGER.info("Hurts to stomp {}!", thisEntity.getName().getString());
				return StompResult.PAINFUL;
			}

			if(this instanceof Saddleable thisSaddleable) {
				if(thisSaddleable.isSaddled() && marioData.getMario().startRiding(thisEntity, false))
					return StompResult.MOUNT;
			}

			boolean damaged = thisEntity.damage(damageSource, damageAmount);
			return damaged ? StompResult.NORMAL : StompResult.RESISTED;
		}
		else return StompResult.FAIL;
	}
}
