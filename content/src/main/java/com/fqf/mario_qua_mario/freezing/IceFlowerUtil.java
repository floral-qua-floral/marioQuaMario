package com.fqf.mario_qua_mario.freezing;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.entity.custom.AbstractMarioProjectileEntity;
import com.fqf.mario_qua_mario.entity.custom.FatalityReceiver;
import com.fqf.mario_qua_mario.entity.custom.IceEncasementEntity;
import com.fqf.mario_qua_mario.util.MQMTags;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class IceFlowerUtil {
	public static final RegistryKey<DamageType> ICEBALL_DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, MarioQuaMario.makeResID("iceball"));
	public static final RegistryKey<DamageType> SHATTER_DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, MarioQuaMario.makeResID("shatter"));
	public static final RegistryKey<DamageType> DIRECT_SHATTER_DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, MarioQuaMario.makeResID("shatter_direct"));

	private static @Nullable IceEncasementEntity.HypotheticalIceEncasement temporaryFatalityReceiver;

	public static @Nullable IceEncasementEntity encase(Entity target) {
		// Players & Ice Encasements may under absolutely no circumstances ever be encased!!!
		if(target instanceof PlayerEntity || target instanceof IceEncasementEntity) {
			return null;
		}

		// Dead or removed entities cannot be encased
		if(!target.isAlive() || (target instanceof LivingEntity livingTarget && livingTarget.isDead()) || target.isRemoved())
			return null;

		IceEncasementEntity encasement = new IceEncasementEntity(target.getWorld(), target,
				temporaryFatalityReceiver == null ? null : temporaryFatalityReceiver.fatalDamageSource);
		target.getWorld().spawnEntity(encasement);

		if(temporaryFatalityReceiver != null && temporaryFatalityReceiver.fatalDamageSource != null)
			encasement.makeFatal(temporaryFatalityReceiver.fatalDamageSource, false);

		endIllegalEncasedInteractions(target);

//		MarioQuaMario.LOGGER.info("Spawned encasement: {}", encasement);
		return encasement;
	}

	public static boolean canEncase(Entity target) {
		return
				target.isAlive()
				&& ((IceEncasementEntity.EncasementOwner) target).mqm$getEncasement() == null
				&& !(target instanceof PlayerEntity)
				&& !(target instanceof IceEncasementEntity)
				&& (target instanceof LivingEntity
						? !target.getType().isIn(MQMTags.CANNOT_BE_ENCASED)
						: target.getType().isIn(MQMTags.ENCASEABLE_NON_LIVING)
				);
	}

	public static AbstractMarioProjectileEntity.ProjectileStrikeResult iceballHit(Entity target, Entity source, Entity attacker) {
		try {
			temporaryFatalityReceiver = new IceEncasementEntity.HypotheticalIceEncasement();
			AbstractMarioProjectileEntity.ProjectileStrikeResult result = target instanceof LivingEntity
					? AbstractMarioProjectileEntity.ProjectileStrikeResult.get(target, () ->
							!target.getType().isIn(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES)
							&& target.damage(new DamageSource(
									target.getWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(IceFlowerUtil.ICEBALL_DAMAGE_TYPE),
									source, attacker
							), 2)
					)
					: new AbstractMarioProjectileEntity.ProjectileStrikeResult(true, true);

			if(result.targetDamaged() && canEncase(target)) {
				// If we still, somehow, fail to encase the target, then we'll just claim it wasn't even damaged.
				// This will never be triggered on client side, because we shouldn't be trying to spawn encasements on
				// that side.
				if(!target.getWorld().isClient() && encase(target) == null)
					return new AbstractMarioProjectileEntity.ProjectileStrikeResult(true, false);
			}

			if(result.targetStruck() && !target.getWorld().isClient()) target.setFrozenTicks(target.getFrozenTicks() + 40);

			return result;
		}
		finally {
			temporaryFatalityReceiver = null;
		}
	}

	public static @Nullable FatalityReceiver getFatalityReceiver(Entity target) {
		IceEncasementEntity encasement = ((IceEncasementEntity.EncasementOwner) target).mqm$getEncasement();
		return encasement == null ? temporaryFatalityReceiver : encasement;
	}

	public static DamageSource makeShatterSource(World world, Entity source, Entity attacker) {
		return new DamageSource(
				world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(
						attacker == null
								? SHATTER_DAMAGE_TYPE
								: DIRECT_SHATTER_DAMAGE_TYPE
				),
				source, attacker
		);
	}

	public static void doEntityTickInjection(Entity entity, CallbackInfo ci) {
		if(((IceEncasementEntity.EncasementOwner) entity).mqm$getEncasement() != null) {
			ci.cancel();
//			entity.baseTick();
			if(entity.age == 0) entity.age = 1;
			if(entity instanceof LivingEntity livingEntity) {
				livingEntity.deathTime = 0;
				if(livingEntity.timeUntilRegen > 0)
					livingEntity.timeUntilRegen--;
				if(livingEntity.hurtTime > 0)
					livingEntity.hurtTime--;
			}

			endIllegalEncasedInteractions(entity);
		}
	}

	public static void endIllegalEncasedInteractions(Entity entity) {
		entity.extinguishWithSound();
		entity.removeAllPassengers();
		entity.dismountVehicle();
	}
}
