package com.fqf.mario_qua_mario.entity.custom;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.entity.MQMEntities;
import com.fqf.mario_qua_mario.util.MQMTags;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class MarioFireballProjectileEntity extends AbstractBouncingMarioProjectileEntity {
	public float angle;
	public float renderYaw;
	public float prevAngle;

	public MarioFireballProjectileEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
	}

	public MarioFireballProjectileEntity(World world, ServerPlayerEntity mario) {
		super(MQMEntities.MARIO_FIREBALL, world, mario);
	}

	@Override
	protected int getTotalBounces() {
		return 10;
	}

	@Override
	protected float getHorizontalSpeed() {
		return 0.7F;
	}

	@Override
	protected double getBounceVel() {
		return 0.475;
	}

	@Override
	protected SoundEvent getWallSound() {
		return MarioSFX.FIREBALL_WALL;
	}

	@Override
	protected double getGravity() {
		return 0.15;
	}

	@Override
	public void tick() {
		super.tick();
		this.prevAngle = this.angle;
		this.angle += 28F;
	}

	private static final RegistryKey<DamageType> DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, MarioQuaMario.makeResID("mario_fireball"));

	@Override public boolean hitEntity(Entity target, Entity source, Entity attacker, Entity soundPlayer) {
		boolean damaged = target.damage(new DamageSource(
				source.getWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(DAMAGE_TYPE),
				source, attacker
		), 5);

		if(damaged && target.getType().isIn(MQMTags.DODGES_MARIO_FIREBALL))
			return false;

		if(!source.getWorld().isClient()) {
			SoundEvent event;
			if(!damaged) event = MarioSFX.FIREBALL_WALL;
			else if(target.isAlive()) event = MarioSFX.FIREBALL_ENEMY;
			else event = MarioSFX.KICK;
			soundPlayer.playSound(event, 1, 1);
		}

		return damaged;
	}

	@Override
	public boolean attemptBlockInteraction(BlockPos target, World world) {
		if(world.getBlockState(target).isIn(MQMTags.DESTROYED_BY_FIREBALL)) {
			world.removeBlock(target, false);
			world.playSound(null, target, MarioSFX.BURN_OBJECT, SoundCategory.BLOCKS, 1, 1);
			return true;
		}
		return false;
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {

	}

	@Override
	public void onSpawnPacket(EntitySpawnS2CPacket packet) {
		super.onSpawnPacket(packet);

		this.renderYaw = -packet.getYaw();
		this.setVelocity(packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ());
	}
}
