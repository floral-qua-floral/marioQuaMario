package com.fqf.mario_qua_mario.entity.custom;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.entity.MQMEntities;
import com.fqf.mario_qua_mario.util.MQMTags;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public abstract class AbstractBouncingMarioProjectileEntity extends AbstractMarioProjectileEntity {
	private int bounces;
	public AbstractBouncingMarioProjectileEntity(EntityType<? extends ProjectileEntity> entityType, World world) {
		super(entityType, world);
		this.bounces = this.getTotalBounces();
	}

	public AbstractBouncingMarioProjectileEntity(World world, ServerPlayerEntity mario) {
		this(MQMEntities.MARIO_FIREBALL, world);
		this.setOwner(mario);
		this.setPosition(
				mario.getX() - (mario.getWidth() + 1) * 0.5 * Math.sin(mario.getYaw() * (Math.PI / 180.0)),
				mario.getY() + mario.getEyeHeight(mario.getPose()) * 0.5F,
				mario.getZ() + (mario.getWidth() + 1) * 0.5 * Math.cos(mario.getYaw() * (Math.PI / 180.0))
		);
		this.setVelocity(
				MathHelper.sin(-mario.getYaw() * MathHelper.RADIANS_PER_DEGREE) * this.getHorizontalSpeed(),
				Math.sin(mario.getPitch() * MathHelper.RADIANS_PER_DEGREE) * -1,
				MathHelper.cos(-mario.getYaw() * MathHelper.RADIANS_PER_DEGREE) * this.getHorizontalSpeed()
		);
		this.setYaw(mario.getYaw());
	}

	protected abstract int getTotalBounces();
	protected abstract float getHorizontalSpeed();

	protected abstract SoundEvent getWallSound();

	@Override
	public void tick() {
		super.tick();

		if(
				!this.getWorld().isClient()
				&& ProjectileUtil.getCollision(this, this::canHit, RaycastContext.ShapeType.OUTLINE) instanceof BlockHitResult blockHitResult
				&& this.attemptBlockInteraction(blockHitResult.getBlockPos(), this.getWorld())
		) {
			this.discard();
		}
		if(!this.getWorld().isClient) MarioQuaMario.LOGGER.info("Tick {}\t\tMotion: {}", this.getWorld().getTime(), this.getVelocity());
		this.hitOrDeflect(ProjectileUtil.getCollision(this, this::canHit));

		this.updateRotation();

		if(this.getY() < this.getWorld().getBottomY()) {
			this.discard();
		}
		else {
			Vec3d vec3d = this.getVelocity();
			this.setVelocity(vec3d.multiply(0.99F));
			this.applyGravity();
			this.setPosition(this.getX() + vec3d.x, this.getY() + vec3d.y, this.getZ() + vec3d.z);
		}
	}

	@Override
	protected void onEntityHit(EntityHitResult entityHitResult) {
		super.onEntityHit(entityHitResult);
		this.hitEntity(entityHitResult.getEntity(), this, this.getOwner(), this);
		if(!this.getWorld().isClient() && !entityHitResult.getEntity().getType().isIn(MQMTags.DODGES_MARIO_FIREBALL))
			this.discard();
	}

	private static final double STEP_HEIGHT = 1.2;

	private void bounce() {
		this.bounces--;
		Vec3d velocity = this.getVelocity();
		this.setVelocity(velocity.withAxis(Direction.Axis.Y, -0.475 * Math.signum(velocity.y)));
		if(!this.getWorld().isClient) this.playSound(this.getWallSound(), 0.233F, 0.75F);
	}

	@Override
	protected void onBlockHit(BlockHitResult result) {
		super.onBlockHit(result);

		boolean canBounce = this.bounces > 0;

		if(canBounce && (result.getSide().getAxis() == Direction.Axis.Y)) {
			// We'll hit a wall or a ceiling, so we can just do completely ordinary bounce logic
			this.bounce();
		}
		else {
			Vec3d velocity = this.getVelocity();
			Vec3d checkAtOffset = new Vec3d(velocity.x * 0, STEP_HEIGHT, velocity.z * 0);
			if(canBounce && this.getWorld().isSpaceEmpty(this, this.getBoundingBox().offset(checkAtOffset))) {
				// Teleport upwards as far as we can possibly step, to the space we just checked is empty
				this.setPosition(this.getPos().add(checkAtOffset));

				// Try to move as far downwards as we just teleported upwards - this should hit the floor, giving the
				// appearance that we snapped up to it and no further.
				// Also we need to make sure this doesn't change the velocity so that we can bounce properly...
				Vec3d preMoveVelocity = this.getVelocity();
				this.move(MovementType.SELF, new Vec3d(0, -STEP_HEIGHT, 0));
				this.setVelocity(preMoveVelocity);

				// Deduct a bounce and give us upward velocity
				this.bounce();
			}
			else {
				// Hit a wall and the space above us is occupied :(
				this.playSound(this.getWallSound(), 0.45F, 1);
				this.discard();
			}
		}
	}
}
