package com.fqf.mario_qua_mario.entity.custom;

import it.unimi.dsi.fastutil.booleans.BooleanBooleanImmutablePair;
import it.unimi.dsi.fastutil.booleans.BooleanBooleanPair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.BooleanSupplier;

public abstract class AbstractMarioProjectileEntity extends ProjectileEntity {
	public AbstractMarioProjectileEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	public abstract boolean hitEntity(Entity target, Entity source, Entity attacker, Entity soundOrigin);
	public abstract boolean attemptBlockInteraction(BlockPos target, World world);

	private static final double MIN_TELEPORT_DISTANCE_TO_COUNT_AS_DODGE = 3;
	public record ProjectileStrikeResult(boolean targetStruck, boolean targetDamaged) {
		public static ProjectileStrikeResult get(Entity target, BooleanSupplier damageApplicator) {
			Vec3d preDamagePos = target.getPos();

			boolean damageReturnedTrue = damageApplicator.getAsBoolean();

			boolean dodged = target.getPos().squaredDistanceTo(preDamagePos) >
					MIN_TELEPORT_DISTANCE_TO_COUNT_AS_DODGE * MIN_TELEPORT_DISTANCE_TO_COUNT_AS_DODGE;

			// If the entity claimed to have been harmed, and we can double-check that, then do. If it was lying, then
			// it probably dodged, even if it hasn't actually moved far enough to count.
			if(damageReturnedTrue) dodged = dodged || (
					target instanceof LivingEntity livingTarget
					&& livingTarget.hurtTime <= 0
			);

			return new ProjectileStrikeResult(!dodged, !dodged && damageReturnedTrue);
		}
	}
}
