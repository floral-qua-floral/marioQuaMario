package com.fqf.mario_qua_mario_content.entity.custom;

import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.entity.ModEntities;
import com.fqf.mario_qua_mario_content.util.MarioContentSFX;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class MarioFireballProjectileEntity extends ProjectileEntity {
	private float rotation;
	private int bounces;
	private boolean prevTickWallUpped;
	public float angle;
	public float renderYaw;
	public float prevAngle;

	private static final float FIREBALL_SPEED = 0.7F;

	public MarioFireballProjectileEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
		this.bounces = 10;
	}

	public MarioFireballProjectileEntity(World world, ServerPlayerEntity mario) {
		this(ModEntities.MARIO_FIREBALL, world);
		this.setOwner(mario);
		this.setPosition(
				mario.getX() - (mario.getWidth() + 1) * 0.5 * Math.sin(mario.getYaw() * (Math.PI / 180.0)),
				 mario.getY() + mario.getEyeHeight(mario.getPose()) * 0.5F,
				mario.getZ() + (mario.getWidth() + 1) * 0.5 * Math.cos(mario.getYaw() * (Math.PI / 180.0))
		);
		this.setVelocity(
				MathHelper.sin(-mario.getYaw() * MathHelper.RADIANS_PER_DEGREE) * FIREBALL_SPEED,
				Math.sin(mario.getPitch() * MathHelper.RADIANS_PER_DEGREE) * -1,
				MathHelper.cos(-mario.getYaw() * MathHelper.RADIANS_PER_DEGREE) * FIREBALL_SPEED
		);
		this.setYaw(mario.getYaw());
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
		HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
		this.hitOrDeflect(hitResult);

		this.updateRotation();

		if (this.getWorld().isOutOfHeightLimit((int) this.getY())) {
			this.discard();
		} else {
			Vec3d vec3d = this.getVelocity();
			this.setVelocity(vec3d.multiply(0.99F));
			this.applyGravity();
			this.setPosition(this.getX() + vec3d.x, this.getY() + vec3d.y, this.getZ() + vec3d.z);
		}
	}

	private static final RegistryKey<DamageType> DAMAGE_TYPE = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, MarioQuaMarioContent.makeResID("mario_fireball"));

	public static boolean hitEntity(Entity target, Entity source, Entity attacker, Entity soundPlayer) {
		boolean damaged = target.damage(new DamageSource(
				source.getWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(DAMAGE_TYPE),
				source, attacker
		), 5);

		if(!source.getWorld().isClient()) {
			SoundEvent event;
			if(!damaged) event = MarioContentSFX.FIREBALL_WALL;
			else if(target.isAlive()) event = MarioContentSFX.FIREBALL_ENEMY;
			else event = MarioContentSFX.KICK;
			soundPlayer.playSound(event, 1, 1);
		}

		return damaged;
	}

	@Override
	protected void onEntityHit(EntityHitResult entityHitResult) {
		super.onEntityHit(entityHitResult);
		hitEntity(entityHitResult.getEntity(), this, this.getOwner(), this);
		if(!this.getWorld().isClient()) this.discard();
	}

	private static final double FIREBALL_STEP_HEIGHT = 1.2;

	private void bounce() {
		this.bounces--;
		Vec3d velocity = this.getVelocity();
		this.setVelocity(velocity.withAxis(Direction.Axis.Y, -0.475 * Math.signum(velocity.y)));
		if(!this.getWorld().isClient) this.playSound(MarioContentSFX.FIREBALL_WALL, 0.233F, 0.75F);
	}

	@Override
	protected void onBlockHit(BlockHitResult result) {
		super.onBlockHit(result);

		boolean canBounce = this.bounces > 0;

		if(canBounce && (result.getSide().getAxis() == Direction.Axis.Y)) {
			this.bounce();
		}
		else {
			Vec3d velocity = this.getVelocity();
			Vec3d checkAtOffset = new Vec3d(velocity.x * 0, FIREBALL_STEP_HEIGHT, velocity.z * 0);
			if(canBounce && this.getWorld().isSpaceEmpty(this, this.getBoundingBox().offset(checkAtOffset))) {
				this.setPosition(this.getPos().add(checkAtOffset));
				this.move(MovementType.SELF, new Vec3d(0, -FIREBALL_STEP_HEIGHT, 0));
				this.bounce();
			}
			else {
				this.playSound(MarioContentSFX.FIREBALL_WALL, 0.45F, 1);
				this.discard();
			}
		}
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
