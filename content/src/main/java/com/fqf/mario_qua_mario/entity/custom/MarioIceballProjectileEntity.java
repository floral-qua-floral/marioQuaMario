package com.fqf.mario_qua_mario.entity.custom;

import com.fqf.mario_qua_mario.entity.MQMEntities;
import com.fqf.mario_qua_mario.freezing.IceFlowerFreezable;
import com.fqf.mario_qua_mario.util.MQMTags;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FlyingItemEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class MarioIceballProjectileEntity extends AbstractBouncingMarioProjectileEntity implements FlyingItemEntity {
	public MarioIceballProjectileEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	public MarioIceballProjectileEntity(World world, ServerPlayerEntity mario) {
		super(MQMEntities.MARIO_ICEBALL, world, mario);
	}

	@Override
	protected int getTotalBounces() {
		return 1;
	}

	@Override
	protected float getHorizontalSpeed() {
		return 0.45F;
	}

	@Override
	protected double getBounceVel() {
		return 1;
	}

	@Override
	protected SoundEvent getWallSound() {
		return MarioSFX.ICEBALL_WALL;
	}

	@Override
	protected double getGravity() {
		return 0.15;
	}

	@Override
	protected boolean canHit(Entity entity) {
		return super.canHit(entity) || (!this.isConnectedThroughVehicle(entity) && ((IceFlowerFreezable) entity).mqm$canBeEncased());
	}

	@Override
	public boolean hitEntity(Entity target, Entity source, Entity attacker, Entity soundOrigin) {
		IceFlowerFreezable freezable = (IceFlowerFreezable) target;
		boolean success = freezable.mqm$iceFlowerHit(source, attacker);
		if(!success) this.playSound(MarioSFX.ICEBALL_WALL, 1, 1);
		return success;
	}

	@Override
	public boolean attemptBlockInteraction(BlockPos target, World world) {
		BlockState state = world.getBlockState(target);
		if(state.isIn(MQMTags.EXTINGUISHED_BY_ICEBALL)) {
			if(state.contains(Properties.LIT)) {
				if(!world.isClient && state.get(Properties.LIT)) {
					world.setBlockState(target, state.with(Properties.LIT, false), Block.NOTIFY_ALL_AND_REDRAW);
					world.emitGameEvent(this.getOwner(), GameEvent.BLOCK_CHANGE, target);
				}
			}
			else {
				world.removeBlock(target, false);
				if(!world.isClient)
					world.emitGameEvent(this.getOwner(), GameEvent.BLOCK_DESTROY, target);
			}

			world.playSound(null, target, MarioSFX.ICEBALL_EXTINGUISH, SoundCategory.BLOCKS, 1, 1);
			return true;
		}
		return false;
	}

	@Override
	public void tick() {
		super.tick();
		this.getWorld().addParticle(ParticleTypes.SNOWFLAKE, false, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
	}

	private static final double REMOVAL_PUFF_RADIUS = 0.4;

	@Override
	public void onRemoved() {
		super.onRemoved();
		if(this.getWorld().isClient) {
			for (int particleCount = 0; particleCount < 6; particleCount++) {
				Random random = this.getRandom();
				Vec3d offset = new Vec3d(
						random.nextTriangular(0, REMOVAL_PUFF_RADIUS),
						random.nextTriangular(0, REMOVAL_PUFF_RADIUS),
						random.nextTriangular(0, REMOVAL_PUFF_RADIUS)
				).multiply(0.25);
				Vec3d pos = this.getPos().add(offset);
				this.getWorld().addParticle(ParticleTypes.SNOWFLAKE, false,
						pos.x, pos.y, pos.z, offset.x, offset.y, offset.z);
			}
		}
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {

	}

	private static final ItemStack DEFAULT_SNOWBALL_STACK = Items.SNOWBALL.getDefaultStack();
	@Override
	public ItemStack getStack() {
		return DEFAULT_SNOWBALL_STACK;
	}
}
