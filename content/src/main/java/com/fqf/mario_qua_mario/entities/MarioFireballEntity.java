package com.fqf.mario_qua_mario.entities;

import net.minecraft.entity.EntityStatuses;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.AbstractFireballEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MarioFireballEntity extends AbstractFireballEntity {
	public MarioFireballEntity(EntityType<? extends MarioFireballEntity> entityType, World world) {
		super(entityType, world);
	}

	public MarioFireballEntity(EntityType<? extends MarioFireballEntity> entityType, double d, double e, double f, Vec3d vec3d, World world) {
		super(entityType, d, e, f, vec3d, world);
	}

	public MarioFireballEntity(EntityType<? extends MarioFireballEntity> entityType, LivingEntity livingEntity, Vec3d vec3d, World world) {
		super(entityType, livingEntity, vec3d, world);
	}



	@Override
	protected void initDataTracker(DataTracker.Builder builder) {

	}

	@Override
	protected void onEntityHit(EntityHitResult entityHitResult) {
		super.onEntityHit(entityHitResult);
		entityHitResult.getEntity().damage(this.getDamageSources().create(DamageTypes.FIREBALL, this, this.getOwner()), 200);
	}

	@Override
	protected void onBlockHit(BlockHitResult blockHitResult) {
		super.onBlockHit(blockHitResult);

		if(blockHitResult.getSide().getAxis() == Direction.Axis.Y) this.setVelocity(this.getVelocity().multiply(1, -1, 1));
		else {
			if(!this.getWorld().isClient) {
				this.getWorld().sendEntityStatus(this, EntityStatuses.PLAY_DEATH_SOUND_OR_ADD_PROJECTILE_HIT_PARTICLES);
				this.discard();
			}
		}
	}

	@Override
	protected void onCollision(HitResult hitResult) {
		super.onCollision(hitResult);
	}
}
