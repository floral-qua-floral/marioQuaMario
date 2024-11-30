package com.floralquafloral.mixin;

import com.floralquafloral.StompableEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Saddleable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Entity.class)
public abstract class EntityStompabilityMixin implements StompableEntity {
	@Override public StompResult qua_mario$stomp(PlayerEntity mario, Identifier stompType, DamageSource damageSource, float amount) {
		Entity self = (Entity) (Object) this;

		if(self instanceof VehicleEntity || (self instanceof Saddleable saddleable && saddleable.isSaddled()))
			if((!(self instanceof MinecartEntity minecart) || minecart.getMinecartType() == AbstractMinecartEntity.Type.RIDEABLE))
				if(mario.startRiding(self, false))
					return StompResult.MOUNT;

		if(getType().isIn(UNSTOMPABLE_ENTITIES)) return StompResult.FAIL;
		if(getType().isIn(HURTS_TO_STOMP_ENTITIES)) return StompResult.PAINFUL;
		if(getType().isIn(IMMUNE_TO_BASIC_STOMP_ENTITIES) && damageSource.isIn(BASIC_STOMPS)) return StompResult.FAIL;

		boolean damaged = damage(damageSource, amount);

		if(damaged) return StompResult.NORMAL;
		else return StompResult.FAIL;
	}

	@Shadow public abstract EntityType<?> getType();
	@Shadow public abstract boolean damage(DamageSource source, float amount);

	@Unique private static final TagKey<EntityType<?>> UNSTOMPABLE_ENTITIES =
			TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("qua_mario:unstompable"));
	@Unique private static final TagKey<EntityType<?>> HURTS_TO_STOMP_ENTITIES =
			TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("qua_mario:hurts_to_stomp"));
	@Unique private static final TagKey<EntityType<?>> IMMUNE_TO_BASIC_STOMP_ENTITIES =
			TagKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of("qua_mario:immune_to_basic_stomp"));

	@Unique private static final TagKey<DamageType> BASIC_STOMPS =
			TagKey.of(RegistryKeys.DAMAGE_TYPE, Identifier.of("qua_mario:basic_stomps"));
}
