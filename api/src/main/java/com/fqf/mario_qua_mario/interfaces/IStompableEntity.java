package com.fqf.mario_qua_mario.interfaces;

import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

/**
 * An entity can implement this interface and override its methods to change Stomp attacks interact with it.
 */
public interface IStompableEntity {
	default StompResult mqm$getStompResult(
			IMarioAuthoritativeData mario,
			Identifier stompType,
			boolean canMount,
			DamageSource damageSource,
			float amount
	) {
		// Only entities can be stomped.
		if(!(this instanceof Entity entity)) return StompResult.FAIL;

		// Mario can mount some entities by falling onto them from above, like how he mounts Yoshi in most games.
		if(canMount && this.mqm$isStompMountable() && mario.getMario().startRiding(entity, false))
			return StompResult.MOUNT;

		// Various datapack-tag-driven interactions
		if(entity.getType().isIn(UNSTOMPABLE_ENTITIES)) return StompResult.FAIL;
		if(entity.getType().isIn(HURTS_TO_STOMP_ENTITIES)) return StompResult.PAINFUL;
		if(entity.getType().isIn(IMMUNE_TO_BASIC_STOMP_ENTITIES) && damageSource.isIn(BASIC_STOMPS)) return StompResult.FAIL;

		// Attempt to apply the damage from the stomp to the entity.
		// If the damage goes through, then Mario should do a normal stomp.
		// If it doesn't, that means the entity is probably immune for whatever reason, and Mario should do a Resisted stomp.
		boolean damaged = entity.damage(damageSource, amount);

		if(damaged) return StompResult.NORMAL;
		else return StompResult.RESISTED;
	}

	default boolean mqm$isStompMountable() {
		if(this instanceof VehicleEntity vehicle) {
			// If we're a vehicle, then:
			//		- If we're a Minecart, then return true if we're an empty minecart (as opposed to, i.e., a Chest Minecart)
			//		- If we're not a Minecart, then return true
			return !(vehicle instanceof MinecartEntity minecart) || minecart.getMinecartType() != AbstractMinecartEntity.Type.RIDEABLE;
		}
		if(this instanceof Saddleable saddleable) {
			// If we're something that can be saddled, then:
			//		- If we're saddled, then return true
			//		- If we're not saddled, then return false
			return saddleable.isSaddled();
		}

		// If we're neither a vehicle nor saddleable, then return false.
		return false;
	}

	TagKey<EntityType<?>> UNSTOMPABLE_ENTITIES =
			TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("mario_qua_mario:unstompable"));
	TagKey<EntityType<?>> HURTS_TO_STOMP_ENTITIES =
			TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("mario_qua_mario:hurts_to_stomp"));
	TagKey<EntityType<?>> IMMUNE_TO_BASIC_STOMP_ENTITIES =
			TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("mario_qua_mario:immune_to_basic_stomp"));

	TagKey<DamageType> BASIC_STOMPS =
			TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of("mario_qua_mario:basic_stomps"));
}
