package com.fqf.mario_qua_mario.entity.custom;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.entity.MQMEntities;
import com.fqf.mario_qua_mario.mixin.freezing.DeathSoundAccessor;
import com.fqf.mario_qua_mario.util.MQMGamerules;
import com.fqf.mario_qua_mario.util.MQMTags;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class IceEncasementEntity extends AbstractSolidMovingEntity implements Ownable, FatalityReceiver {
	private static final TrackedData<Byte> PHYSICS =
			DataTracker.registerData(IceEncasementEntity.class, TrackedDataHandlerRegistry.BYTE);
	private static final TrackedData<Boolean> IS_SHAKING =
			DataTracker.registerData(IceEncasementEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	public static final TrackedData<Byte> MINING_PROGRESS =
			DataTracker.registerData(IceEncasementEntity.class, TrackedDataHandlerRegistry.BYTE);

	public @Nullable Object cuboidForClientRendering;
	private @Nullable Entity owner;
	private int lookingForOwner;
	private EncasementPhysicsType physics;
	private byte airTicks;
	private float timeUntilEscape;
	private boolean isFatal;
	private final Set<Entity> noClippingEntities = Collections.newSetFromMap(new WeakHashMap<>());

	public IceEncasementEntity(EntityType<? extends IceEncasementEntity> type, World world) {
		super(type, world);

		this.intersectionChecked = true;
		this.lookingForOwner = 0;
		this.airTicks = 70;
		this.physics = EncasementPhysicsType.NO_PHYSICS;

		this.timeUntilEscape = Float.POSITIVE_INFINITY;
	}

	public IceEncasementEntity(World world, Entity owner, @Nullable DamageSource fatalDamageSource) {
		this(MQMEntities.ICE_ENCASEMENT, world);
		this.setOwner(owner);

		float downwardInflation = ((EncasementSizeProvider) owner).mqm$getDownwardInflation();
		this.setPosition(owner.getPos().add(0, downwardInflation, 0));
		if(downwardInflation != 0)
			this.move(MovementType.SELF, new Vec3d(0, -downwardInflation, 0));

		if(fatalDamageSource != null)
			this.makeFatal(fatalDamageSource, false);

		boolean suspend = !owner.isOnGround();

		double threshold = world.getGameRules().get(MQMGamerules.MOVING_ICE_THRESHOLD).get();
		if(threshold > 0) {
			double thresholdSquared = threshold * threshold;

			// Would it ever make sense to use lengthSquared instead of horizontalLengthSquared? Definitely not
			// under most circumstances - otherwise the measured speed of sprint-jumping would fluctuate over time -
			// but what about for entities whose motion is primarily vertical? Maybe not; feels odd for a
			// fast-falling mob to skip the air suspend state.
			if(owner.getVelocity().horizontalLengthSquared() > thresholdSquared) {
				suspend = false;
				this.setVelocity(owner.getVelocity());
			}
		}

		this.setPhysics(suspend ? EncasementPhysicsType.TEMPORARILY_SUSPENDED : EncasementPhysicsType.GRAVITY_PHYSICS);

		if(owner instanceof LivingEntity livingOwner) {
			this.timeUntilEscape =  Math.max(3, 225 - livingOwner.getHealth() * 10) + livingOwner.getFrozenTicks();
		}
	}

	@Override
	public Packet<ClientPlayPacketListener> createSpawnPacket(EntityTrackerEntry entityTrackerEntry) {
		return new EntitySpawnS2CPacket(this, entityTrackerEntry, this.owner == null ? 0 : this.owner.getId());
	}

	@Override
	public void onSpawnPacket(EntitySpawnS2CPacket packet) {
		super.onSpawnPacket(packet);
		this.lookingForOwner = packet.getEntityData();
		this.attemptFindOwner();
	}

	@Override
	public void tick() {
		Entity owner = this.getOwner();
		if(owner == null) {
			this.attemptFindOwner();
			owner = this.getOwner();
		}
		if(owner != null) {
			if(owner.isRemoved()) {
				this.remove(owner.getRemovalReason());
				return;
			}
			owner.refreshPositionAndAngles(this.getPos(), owner.getYaw(), owner.getPitch());
		}

		super.tick();

		if(this.timeUntilEscape <= 0) {
			this.thaw();
		}
	}

	private void setPhysics(EncasementPhysicsType physics) {
		if(!this.getWorld().isClient())
			this.getDataTracker().set(PHYSICS, (byte) physics.ordinal());
	}

	@Override
	protected void updateVelocity() {
		this.setPhysics(this.physics.tick(this));
	}

	private void attemptFindOwner() {
		if(!this.getWorld().isClient())
			throw new IllegalStateException("Server tried to look for Encasement's owner using networking ID -" +
					" something has gone terribly wrong?!");

		Entity prospectiveOwner = this.getWorld().getEntityById(this.lookingForOwner);
		if(prospectiveOwner != null && prospectiveOwner != this) {
			this.lookingForOwner = 0;
			this.setOwner(prospectiveOwner);
			MarioQuaMario.LOGGER.info("Identified ice encasement owner: {}", prospectiveOwner);
		}
	}

	private void setOwner(Entity owner) {
		if(owner instanceof PlayerEntity) throw new IllegalArgumentException("Encasing players is not supported!!!");

		this.owner = owner;
		if(owner != null) {
			((EncasementOwner) owner).mqm$setEncasement(this);
			this.calculateDimensions();
		}
	}

	@Override
	public void calculateDimensions() {
		super.calculateDimensions();
		this.cuboidForClientRendering = null;
	}

	@Override
	public @Nullable Entity getOwner() {
		return this.owner;
	}

	public boolean isFatal() {
		return this.getOwner() != null && this.isFatal;
	}

	@Override
	public boolean makeFatal(DamageSource source, boolean force) {
		if(this.isFatal() || !canTriggerFatalFreeze(source, force)) return false;

		Entity owner = this.getOwner();
		if(owner instanceof LivingEntity livingOwner) {
			livingOwner.onDeath(source);
			livingOwner.playSound(((DeathSoundAccessor) livingOwner).invokeGetDeathSound());
			if(owner instanceof MobEntity mobOwner) {
				mobOwner.setPersistent();
			}
		}
		this.setFatalStatus();
		return true;
	}

	public void setFatalStatus() {
		this.isFatal = true;
		this.timeUntilEscape = Float.POSITIVE_INFINITY;
	}

	public static boolean canTriggerFatalFreeze(DamageSource source, boolean force) {
		return force || !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY);
	}

	private static final int INSTANTANEOUS_UPDATES_UPON_FATAL_THAW = 2000;
	public boolean thaw() {
		this.discard();

		this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), MarioSFX.ICE_ESCAPE, SoundCategory.BLOCKS, 1, 1);
		if(this.getWorld() instanceof ServerWorld serverWorld) {
			serverWorld.spawnParticles(
					new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.ICE.getDefaultState()),
					this.getX(),
					this.getY() + this.getHeight() / 2,
					this.getZ(),
					(int) MathHelper.clamp((MathHelper.square(this.getWidth()) * this.getHeight()) * 64, 1, 200),
					this.getWidth() / 3.5,
					this.getHeight() / 3.5,
					this.getWidth() / 3.5,
					0.15
			);
		}

		Entity owner = this.getOwner();
		if(owner != null) {
			owner.setVelocity(this.getVelocity());
			owner.fallDistance = this.fallDistance;

			if(this.isFatal()) {
				if(owner instanceof LivingEntity livingOwner) {
					livingOwner.setHealth(0);
					livingOwner.deathTime = 0;
					for(int sprintedTicks = 0; sprintedTicks < INSTANTANEOUS_UPDATES_UPON_FATAL_THAW; sprintedTicks++) {
						((DeathSoundAccessor) livingOwner).invokeUpdatePostDeath();
					}
					if(!owner.isRemoved()) {
						MarioQuaMario.LOGGER.warn("Entity {} was not removed even after simulating {} post-death ticks." +
								" Removing manually...", owner, INSTANTANEOUS_UPDATES_UPON_FATAL_THAW);
						owner.remove(RemovalReason.KILLED);
					}
				}
				else {
					owner.remove(RemovalReason.KILLED);
				}
			}
		}

		return false;
	}

	public boolean shatter(@Nullable DamageSource originalSource, float amount) {
		return this.thaw();
	}

	@Override
	public void remove(RemovalReason reason) {
		super.remove(reason);
		if(reason.shouldDestroy() && this.isFatal())
			Objects.requireNonNull(this.getOwner()).remove(reason);
	}

	private void setShaking(boolean isShaking) {
		if(!this.getWorld().isClient()) {
			if(isShaking && !this.getDataTracker().get(IS_SHAKING))
				this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), MarioSFX.ICE_RUMBLE, SoundCategory.BLOCKS, 1, 1);
			this.getDataTracker().set(IS_SHAKING, isShaking);
		}
	}

	public boolean isShaking() {
		return this.getDataTracker().get(IS_SHAKING);
	}

	public float getVisibleMiningProgress() {
		return ((float) this.getDataTracker().get(MINING_PROGRESS) - Byte.MIN_VALUE) / (((float) Byte.MAX_VALUE) - Byte.MIN_VALUE);
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		return !this.isInvulnerableTo(source) && this.shatter(source, amount);
	}

	@Override
	public boolean isInvulnerableTo(DamageSource damageSource) {
		return super.isInvulnerableTo(damageSource) || damageSource.isIn(MQMTags.ENCASED_ENTITIES_IGNORE);
	}

	public boolean interceptDamageToOwner(DamageSource source, float amount) {
		return source.isIn(MQMTags.ENCASED_ENTITIES_IGNORE) || this.shatter(source, amount);
	}

	@Override
	public EntityDimensions getDimensions(EntityPose pose) {
		Entity owner = this.getOwner();
		if(owner == null) return super.getDimensions(pose);

		return ((EncasementSizeProvider) owner).mqm$getEncasementDimensions();
	}

	@Override
	protected void initDataTracker(DataTracker.Builder builder) {
		builder.add(PHYSICS, (byte) 0);
		builder.add(IS_SHAKING, false);
		builder.add(MINING_PROGRESS, Byte.MIN_VALUE);
	}

	@Override
	public void onRemoved() {
		super.onRemoved();
		if(this.owner != null) {
			((EncasementOwner) this.owner).mqm$setEncasement(null);
		}
	}

	@Override
	public boolean canHit() {
		return true;
	}

	@Override
	protected double getGravity() {
		return 0.04;
	}

	@Override
	public boolean canUsePortals(boolean allowVehicles) {
		return false;
	}

	@Override
	public boolean canTeleportBetween(World from, World to) {
		return from.equals(to);
	}

	@Override
	public boolean isPushedByFluids() {
		return super.isPushedByFluids();
	}

	@Override
	public boolean collidesWith(Entity other) {
		return super.collidesWith(other) && !other.equals(this.getOwner());
	}

	@Override
	public void onTrackedDataSet(TrackedData<?> data) {
		super.onTrackedDataSet(data);
		if(PHYSICS.equals(data))
			this.physics = EncasementPhysicsType.values()[this.getDataTracker().get(PHYSICS)];
	}

	@Override
	public boolean saveNbt(NbtCompound nbt) {
		// Only save to disk if we don't have an owner. If we do, that owner is responsible for creating us next time.
		return this.getOwner() == null && super.saveNbt(nbt);
	}

	public static final String PHYSICS_NBT_KEY = "physics";
	public static final String FATALITY_NBT_KEY = "is_fatal";
	public static final String AIR_TICKS_NBT_KEY = "air_suspension_ticks";
	public static final String TIME_UNTIL_ESCAPE_NBT_KEY = "time_until_thaw";

	@Override protected void writeCustomDataToNbt(NbtCompound nbt) {
		nbt.putInt(PHYSICS_NBT_KEY, this.physics.ordinal());
		nbt.putBoolean(FATALITY_NBT_KEY, this.isFatal);
		nbt.putInt(AIR_TICKS_NBT_KEY, this.airTicks);
		nbt.putFloat(TIME_UNTIL_ESCAPE_NBT_KEY, this.timeUntilEscape);
	}

	@Override protected void readCustomDataFromNbt(NbtCompound nbt) {
		this.setPhysics(EncasementPhysicsType.values()[MathHelper.clamp(nbt.getInt(PHYSICS_NBT_KEY), 0, EncasementPhysicsType.values().length - 1)]);
		if(nbt.getBoolean(FATALITY_NBT_KEY)) this.setFatalStatus();
		this.airTicks = nbt.getByte(AIR_TICKS_NBT_KEY);
		this.timeUntilEscape = nbt.getFloat(TIME_UNTIL_ESCAPE_NBT_KEY);
	}

	public static class HypotheticalIceEncasement implements FatalityReceiver {
		public @Nullable DamageSource fatalDamageSource;

		@Override
		public boolean makeFatal(DamageSource source, boolean force) {
			if(this.fatalDamageSource == null && canTriggerFatalFreeze(source, force)) {
				this.fatalDamageSource = source;
				return true;
			}
			return false;
		}
	}

	public interface EncasementOwner {
		@Nullable IceEncasementEntity mqm$getEncasement();
		void mqm$setEncasement(@Nullable IceEncasementEntity encasement);
	}

	public interface EncasementSizeProvider {
		default double mqm$getEncasementYOffset() {
			return 0;
		}

		default float mqm$getHorizontalInflation() {
			return 0.3F;
		}

		default float mqm$getUpwardInflation() {
			return 0.25F;
		}
		default float mqm$getDownwardInflation() {
			return 0;
		}

		default EntityDimensions mqm$getEncasementDimensions() {
			Entity thisEntity = (Entity) this;
			EntityDimensions original = thisEntity.getDimensions(thisEntity.getPose());

			float originalWidth = original.width(), originalHeight = original.height();
			float desiredWidth = originalWidth + this.mqm$getHorizontalInflation();
			float desiredHeight = originalHeight + this.mqm$getUpwardInflation() + this.mqm$getDownwardInflation();

			return original
					.scaled(desiredWidth / originalWidth, desiredHeight / originalHeight)
					.withEyeHeight(original.eyeHeight() + this.mqm$getDownwardInflation());
		}
	}

	private enum EncasementPhysicsType {
		NO_PHYSICS {
			@Override public EncasementPhysicsType tick(IceEncasementEntity encasement) {
				encasement.airTicks = 0;
				encasement.setVelocity(Vec3d.ZERO);
				encasement.timeUntilEscape--;
				encasement.setShaking(encasement.timeUntilEscape < 10);
				return NO_PHYSICS;
			}
		},
		TEMPORARILY_SUSPENDED {
			@Override public EncasementPhysicsType tick(IceEncasementEntity encasement) {
				boolean isFinished =
						encasement.airTicks-- <= 0
						|| encasement.isOnGround()
						|| encasement.getVelocity().lengthSquared() > 0
						|| encasement.isTouchingWater();

				encasement.setShaking(encasement.airTicks < 10 && !isFinished);
				return isFinished ? GRAVITY_PHYSICS : TEMPORARILY_SUSPENDED;
			}
		},
		GRAVITY_PHYSICS {
			@Override public EncasementPhysicsType tick(IceEncasementEntity encasement) {
				double immersionPercent = MathHelper.clamp(
						encasement.getFluidHeight(FluidTags.WATER) / encasement.getHeight(),
						0, 1
				);

				float horizontalDrag, verticalDrag;

				if(immersionPercent > 0.7) {
					// Float upward
					encasement.timeUntilEscape -= SLOW_ESCAPE_PROGRESS;
					double yVel = encasement.getVelocity().getY();
					encasement.addVelocity(0, yVel < 0 ? 0.08 : 0.04, 0);
					horizontalDrag = 0.865F;
					verticalDrag = 0.9F;
				}
				else if(immersionPercent > 0.6) {
					// Equilibrium
					encasement.timeUntilEscape -= SLOW_ESCAPE_PROGRESS;
					horizontalDrag = 0.865F;
					verticalDrag = 0.8F;
				}
				else if(immersionPercent > 0) {
					// Sink
					encasement.timeUntilEscape -= SLOW_ESCAPE_PROGRESS;
					encasement.applyGravity();
					horizontalDrag = 0.865F;
					verticalDrag = 0.9F;
				}
				else if(encasement.isOnGround()) {
					// Grounded movement
					encasement.timeUntilEscape--;
					encasement.setShaking(encasement.timeUntilEscape < 10);
					encasement.applyGravity();

					Block supportingBlock = encasement.getWorld().getBlockState(encasement.getVelocityAffectingPos()).getBlock();
					horizontalDrag = (1 + Blocks.ICE.getSlipperiness() + supportingBlock.getSlipperiness()) * 0.98F / 3;
					verticalDrag = 0.99F;
				}
				else {
					// Airborne movement
					encasement.timeUntilEscape -= SLOW_ESCAPE_PROGRESS;
					encasement.applyGravity();

					horizontalDrag = (1 + Blocks.ICE.getSlipperiness() * 0.98F) / 2;
					verticalDrag = 0.99F;
				}

				encasement.setVelocity(encasement.getVelocity().multiply(horizontalDrag, verticalDrag, horizontalDrag));

				return GRAVITY_PHYSICS;
			}
		};

		private static final float SLOW_ESCAPE_PROGRESS = 0.05F;
		public abstract EncasementPhysicsType tick(IceEncasementEntity encasement);
	}
}
