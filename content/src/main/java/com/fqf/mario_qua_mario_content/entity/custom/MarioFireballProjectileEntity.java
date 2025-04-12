package com.fqf.mario_qua_mario_content.entity.custom;

import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.entity.ModEntities;
import com.fqf.mario_qua_mario_content.util.MarioContentSFX;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
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
	public float angle;
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

	public float getRenderingRotation() {
		this.rotation += 0.5F;
		if(this.rotation >= 360) this.rotation = 0;
		return this.rotation;
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

	@Override
	protected void onEntityHit(EntityHitResult entityHitResult) {
		super.onEntityHit(entityHitResult);
		entityHitResult.getEntity().damage(new DamageSource(
				this.getWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(DAMAGE_TYPE),
				this, this.getOwner()
		), 5);

		if(!this.getWorld().isClient()) {
			this.playSound(entityHitResult.getEntity().isAlive() ? MarioContentSFX.FIREBALL_ENEMY : MarioContentSFX.KICK, 1, 1);
			this.discard();
		}
	}

	private boolean testRaisedCollision() {
		Vec3d pos = this.getPos();
		this.setPosition(pos.offset(Direction.UP, 1));
		HitResult result = ProjectileUtil.getCollision(this, this::canHit);
		this.setPosition(pos);
		return result instanceof BlockHitResult;
	}

	private boolean isEmptyAtHeight(double height) {
		return this.getWorld().isSpaceEmpty(this, this.getBoundingBox().offset(0, height - this.getY(), 0));
	}

	private static final double FIREBALL_STEP_HEIGHT = 1.2;

	@Override
	protected void onBlockHit(BlockHitResult result) {
		super.onBlockHit(result);

//		if(isEmptyAtHeight(this.getY())) return;

		double targetY = this.getY();
		MarioQuaMarioContent.LOGGER.info("Collision at height {}", this.getWorld().getBlockState(result.getBlockPos()));
		if(this.bounces > 0 && (
				// FIXME fireballs shouldn't break when they hit the side of a block if they could instead hit the top of it and continue
//				isEmptyAtHeight(this.getY())
//				|| isEmptyAtHeight(this.getY() + FIREBALL_STEP_HEIGHT)
				result.getSide().getAxis() == Direction.Axis.Y// ||
				//this.getWorld().isSpaceEmpty(this, this.getBoundingBox().offset(0, targetY - this.getY(), 0))
//				!testRaisedCollision()
//				!this.getWorld().getBlockState(result.getBlockPos().offset(Direction.UP)).isSolidSurface(this.getWorld(), result.getBlockPos(), this, result.getSide())
//				this.getWorld().isBlockSpaceEmpty(this, this.getBoundingBox().offset(0, 1, 0))
		)) {
			this.bounces--;
			Vec3d velocity = this.getVelocity();
			this.setVelocity(velocity.withAxis(Direction.Axis.Y, -0.4 * Math.signum(velocity.y)));
//			this.setPosition(this.getX(), , this.getZ());
//			this.move(MovementType.SELF, new Vec3d(0, -FIREBALL_STEP_HEIGHT, 0));
			if(!this.getWorld().isClient) this.playSound(MarioContentSFX.FIREBALL_WALL, 0.233F, 0.75F);
		}
		else {
			MarioQuaMarioContent.LOGGER.info("Is not empty at height {}", targetY);
			this.playSound(MarioContentSFX.FIREBALL_WALL, 0.45F, 1);
			this.discard();
		}
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {

	}

	@Override
	public void onSpawnPacket(EntitySpawnS2CPacket packet) {
		super.onSpawnPacket(packet);

		this.setYaw(packet.getYaw());
		this.setVelocity(packet.getVelocityX(), packet.getVelocityY(), packet.getVelocityZ());
	}
}
