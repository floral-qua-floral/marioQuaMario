package com.fqf.mario_qua_mario.util;

import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.collision_attacks.Stomp;
import com.fqf.mario_qua_mario.entity.custom.FatalityReceiver;
import com.fqf.mario_qua_mario.entity.custom.IceEncasementEntity;
import com.fqf.mario_qua_mario.freezing.IceEncasingEvents;
import com.fqf.mario_qua_mario.freezing.IceFlowerUtil;
import com.fqf.mario_qua_mario.mixin.freezing.ZombieVillagerAccessorMixin;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.*;
import net.minecraft.entity.passive.StriderEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class MQMEventListeners {
	public static void register() {
		// Encasements shield against damage
		ServerLivingEntityEvents.ALLOW_DAMAGE.register(((entity, source, amount) -> {
			IceEncasementEntity encasement = ((IceEncasementEntity.EncasementOwner) entity).mqm$getEncasement();
			return encasement == null || !encasement.interceptDamageToOwner(source, amount);
		}));

		// Fatal freezing
		ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
			FatalityReceiver receiver = IceFlowerUtil.getFatalityReceiver(entity);
			if(receiver != null && receiver.makeFatal(damageSource, false)) {
				entity.setHealth(0.01F);
				return true;
			}
			return true;
		});

		// Stomp Guard
		ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if(entity instanceof ServerPlayerEntity mario) {
				CfaData data = mario.cfa$getCfaData();
				if(data.isEnabled() && data.hasPower(Powers.STOMP_GUARD) && source.isDirect() && !source.isIn(MQMTags.BYPASSES_STOMP_GUARD)) {
					Entity attacker = source.getAttacker();
					if(attacker != null) {
						double marioY;
						if(data.retrieveStateData(MarioVars.class).stompGuardRemainingTicks > 0)
							marioY = Math.max(mario.getY(), data.retrieveStateData(MarioVars.class).stompGuardMinHeight);
						else
							marioY = mario.getY();

						return !Stomp.collidingFromTop(attacker, mario, marioY, new Vec3d(0, -1, 0), false)
								&& (!(attacker instanceof EnderDragonEntity) || !(mario.getY() > attacker.getY() + attacker.getHeight() / 2));
					}
				}
			}

			return true;
		});

		// Striders become cold when encased; primarily intended to deal with loading a Strider that is already encased
		IceEncasingEvents.POST_ENCASEMENT.register((entity, encasement) -> {
			if(entity instanceof StriderEntity strider)
				strider.setCold(true);
		});

		// Skeletons convert to Strays upon thawing
		IceEncasingEvents.THAW_CONVERSION.register((original, encasement) -> {
			if(original instanceof SkeletonEntity skeleton) {
				skeleton.getWorld().syncWorldEvent(null, WorldEvents.SKELETON_CONVERTS_TO_STRAY, skeleton.getBlockPos(), 0);
				return convertAndCopyHealthPercentage(skeleton, EntityType.STRAY);
			}
			else if(original instanceof ZombieEntity zombie && !(original instanceof ZombieVillagerEntity)) {
				EntityType<? extends MobEntity> frostbiteType = getFirstRegisteredEntityType(
						Identifier.ofVanilla("frostbite"),
						Identifier.of("wich", "frozen_zombie"),
						Identifier.of("zombie_variants", "frozen_zombie")
				);
				if(frostbiteType != null && !original.getType().equals(frostbiteType))
					return convertAndCopyHealthPercentage(zombie, frostbiteType);
			}
			else if(original instanceof ZombieVillagerEntity zombieVillager) {
				EntityType<? extends MobEntity> frozenZombieVillagerType = getFirstRegisteredEntityType(
						Identifier.of("wich", "frozen_zombie_villager")
				);
				if(frozenZombieVillagerType != null && !original.getType().equals(frozenZombieVillagerType))
					return convertAndCopyHealthPercentage(zombieVillager, frozenZombieVillagerType);
			}
			return original;
		});
	}

	private static @Nullable EntityType<? extends MobEntity> getFirstRegisteredEntityType(Identifier... ids) {
		for(Identifier id : ids) {
			if(Registries.ENTITY_TYPE.containsId(id)) {
				try {
					//noinspection unchecked
					return (EntityType<? extends MobEntity>) Registries.ENTITY_TYPE.get(id);
				}
				catch(ClassCastException classCastException) {
					MarioQuaMario.LOGGER.error("{} is registered as an entity type, but it doesn't extend MobEntity?!", id);
				}
			}
		}
		return null;
	}

	private static Entity convertAndCopyHealthPercentage(MobEntity original, EntityType<? extends MobEntity> to) {
		LivingEntity result = original.convertTo(to, true);
		if(result != null) {
			result.setHealth(result.getMaxHealth() * (original.getHealth() / original.getMaxHealth()));
			if(
					original instanceof ZombieVillagerEntity originalZombieVillager
					&& result instanceof ZombieVillagerEntity resultantZombieVillager
			) {
				// Is anyone ever going to notice that I made this work???
				resultantZombieVillager.setVillagerData(originalZombieVillager.getVillagerData());
				ZombieVillagerAccessorMixin accessor = (ZombieVillagerAccessorMixin) originalZombieVillager;
				resultantZombieVillager.setGossipData(accessor.getGossipData());
				resultantZombieVillager.setOfferData(accessor.getOfferData());
				resultantZombieVillager.setXp(accessor.getXp());
			}
		}
		return result;
	}
}
