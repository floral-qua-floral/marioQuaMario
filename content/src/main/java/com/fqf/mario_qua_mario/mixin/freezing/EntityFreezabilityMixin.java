package com.fqf.mario_qua_mario.mixin.freezing;

import com.fqf.mario_qua_mario.freezing.IceFlowerFreezable;
import com.fqf.mario_qua_mario.freezing.IceFlowerUtil;
import com.fqf.mario_qua_mario.util.MQMTags;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.Blocks;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageSources;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityFreezabilityMixin implements IceFlowerFreezable {
	@Unique private static final TrackedData<Vector3f> ENCASEMENT_POS =
			DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.VECTOR3F);
	@Unique private static final Vector3f INVALID_POS = new Vector3f(Float.NaN);

	@Unique private static final TrackedData<Byte> MINING_PROGRESS =
			DataTracker.registerData(Entity.class, TrackedDataHandlerRegistry.BYTE);

	@Unique private int airSuspendTime;
	@Unique private final Set<ServerPlayerEntity> miners = new HashSet<>();

	@Shadow public abstract DataTracker getDataTracker();

	@Shadow public abstract EntityType<?> getType();
	@Shadow public abstract void calculateDimensions();
	@Shadow public abstract Box getBoundingBox();

	@Shadow public abstract double getX();
	@Shadow public abstract double getY();
	@Shadow public abstract double getZ();
	@Shadow public abstract World getWorld();

	@Shadow public abstract int getFrozenTicks();
	@Shadow public abstract int getMinFreezeDamageTicks();
	@Shadow public abstract void setFrozenTicks(int frozenTicks);

	@Shadow public float fallDistance;

	@Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);
	@Shadow public abstract void extinguishWithSound();
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	@Shadow public abstract boolean isInvulnerableTo(DamageSource damageSource);
	@Shadow public abstract boolean damage(DamageSource source, float amount);
	@Shadow public abstract void removeAllPassengers();

	@Shadow public abstract Vec3d getPos();
	@Shadow public abstract void refreshPositionAndAngles(double x, double y, double z, float yaw, float pitch);
	@Shadow public abstract void move(MovementType movementType, Vec3d movement);

	@Shadow public abstract Vec3d getVelocity();
	@Shadow public abstract void setVelocity(Vec3d velocity);

	@Shadow public abstract float getYaw();
	@Shadow public abstract float getPitch();

	@Shadow public abstract float getHeight();
	@Shadow public abstract float getWidth();

	@Shadow public abstract boolean isSubmergedInWater();
	@Shadow public abstract boolean isTouchingWater();
	@Shadow public abstract boolean isOnGround();

	@Shadow public double prevX, prevY, prevZ;
	@Shadow public double lastRenderX, lastRenderY, lastRenderZ;

	@Shadow public boolean verticalCollision;

	@Shadow public boolean noClip;

	@Shadow public abstract EntityPose getPose();

	@Shadow public abstract DamageSources getDamageSources();

	@Shadow public abstract boolean isRemoved();

	@WrapOperation(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/Entity;initDataTracker(Lnet/minecraft/entity/data/DataTracker$Builder;)V"
			)
	)
	private void initEncasementStatus(Entity instance, DataTracker.Builder builder, Operation<Void> original) {
		builder.add(ENCASEMENT_POS, INVALID_POS);
		builder.add(MINING_PROGRESS, Byte.MIN_VALUE);
		original.call(instance, builder);
	}

	@ModifyReturnValue(method = "isCollidable", at = @At("TAIL"))
	private boolean makeCollidable(boolean original) {
		return original || this.mqm$isEncased();
	}

	@WrapMethod(method = "collidesWith")
	private boolean collideWithOthers(Entity other, Operation<Boolean> original) {
		Entity me = (Entity) (Object) this;
		return original.call(other) || (this.mqm$isEncased() && BoatEntity.canCollide(me, other));
	}

	@Override
	public boolean mqm$canBeEncased() {
		return false;
	}

	@Override
	public boolean mqm$isEncased() {
		return !Float.isNaN(this.getDataTracker().get(ENCASEMENT_POS).x);
	}

	@Override
	public boolean mqm$tickEncased() {
		if(this.mqm$isEncased()) {
			Vec3d pos = this.getPos();
			this.prevX = pos.x; this.prevY = pos.y; this.prevZ = pos.z;
			this.lastRenderX = pos.x; this.lastRenderY = pos.y; this.lastRenderZ = pos.z;

			double horizontalSpeedConservation;
			if(this.noClip) {
				this.setVelocity(Vec3d.ZERO);
				horizontalSpeedConservation = 0;
				this.airSuspendTime = AIR_HANG_TICKS + 1;
			}
			else if(this.isSubmergedInWater()) {
				this.setVelocity(this.getVelocity().add(0, 0.05, 0));
				this.airSuspendTime = AIR_HANG_TICKS + 1;
				horizontalSpeedConservation = 0.8;
			}
			else if(this.isOnGround()) {
				this.setVelocity(this.getVelocity().withAxis(Direction.Axis.Y, -0.1));
				this.airSuspendTime = AIR_HANG_TICKS + 1;
				horizontalSpeedConservation = 0.9;
			}
			else if(this.airSuspendTime < AIR_HANG_TICKS) {
				if(this.airSuspendTime++ == AIR_HANG_RUMBLE_TICKS) this.rumble();
				horizontalSpeedConservation = 0;
			}
			else if(this.isTouchingWater()) {
				this.setVelocity(this.getVelocity().add(0, -0.075, 0));
				horizontalSpeedConservation = 0.8;
			}
			else {
				this.setVelocity(this.getVelocity().add(0, -0.365, 0));
				horizontalSpeedConservation = 1;
			}

			this.setVelocity(this.getVelocity().multiply(horizontalSpeedConservation, 0.8, horizontalSpeedConservation));
			this.move(MovementType.SELF, this.getVelocity());

			return false;
		}
		return true;
	}

	@Unique private static final int AIR_HANG_TICKS = 70;
	@Unique private static final int AIR_HANG_RUMBLE_TICKS = 60;

	@Unique protected void rumble() {
		this.getWorld().playSound(
				null,
				this.getX(), this.getY(), this.getZ(),
				MarioSFX.ICE_RUMBLE, SoundCategory.BLOCKS,
				1, 1
		);
	}

	@Override
	public boolean mqm$iceFlowerHit(@NotNull Entity source, @NotNull Entity attacker) {
		return false;
	}

	@Override
	public boolean mqm$encase() {
		if(!this.mqm$canBeEncased()) return false;

//		this.move(MovementType.SELF, this.getVelocity().multiply(-1));
		this.setVelocity(Vec3d.ZERO);
		this.fallDistance = 0;
		this.extinguishWithSound();
		this.setFrozenTicks(Math.min(this.getMinFreezeDamageTicks() - 1, this.getFrozenTicks() + 20));
		this.removeAllPassengers();

		this.getDataTracker().set(ENCASEMENT_POS, new Vector3f((float) this.prevX, (float) this.prevY, (float) this.prevZ));
		this.airSuspendTime = 0;

		this.playSound(MarioSFX.ICEBALL_ENEMY, 1, 1);
		this.mqm$resetMiningProgress();

		return true;
	}

	@Override
	public boolean mqm$thaw() {
		if(!this.mqm$isEncased()) return false;

		this.getDataTracker().set(ENCASEMENT_POS, INVALID_POS);

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

		this.mqm$resetMiningProgress();

		return true;
	}

	@Override
	public boolean mqm$attemptFatalFreeze(DamageSource source) {
		return false;
	}

	@Override
	public boolean mqm$shatter(DamageSource source, float amount) {
		if(
				!this.mqm$isEncased()
				|| source.isIn(MQMTags.IS_SHATTER)
		) return false;

		float damage = this.calculateShatterDamage(source, amount);
		if(damage == 0) return false;

		if(
				this.applyShatterDamage(IceFlowerUtil.makeShatterSource(this.getWorld(), source.getSource(), source.getAttacker()), damage)
				&& this.mqm$thaw()
		) {
			this.getWorld().playSound(
					null, this.getX(), this.getY(), this.getZ(),
					SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS,
					1, 0.8F
			);
			return true;
		}
		return false;
	}

	@Unique protected float calculateShatterDamage(@Nullable DamageSource source, float amount) {
		if(source == null)
			return this.getIceHardness() * 8;
		else if(source.isIn(MQMTags.MULTIPLIES_SHATTER_DAMAGE))
			return Math.max(amount * 2.5F, 10);
		else if(source.isIn(MQMTags.DOES_NOT_SHATTER_ICE))
			return 0;
		else
			return amount * 1.5F;
	}

	@Unique protected boolean applyShatterDamage(DamageSource shatterSource, float damage) {
		return this.damage(shatterSource, damage);
	}

	@Override
	public boolean mqm$isShaking() {
		return !this.isOnGround() && this.airSuspendTime > AIR_HANG_RUMBLE_TICKS && this.airSuspendTime < AIR_HANG_TICKS;
	}

	@Unique private static final float BYTE_RANGE = Byte.MAX_VALUE - Byte.MIN_VALUE;

	@Override
	public boolean mqm$canMine() {
		return this.mqm$isEncased();
	}

	@Override
	public void mqm$updateMiningProgress(ServerPlayerEntity miner) {
		float newProgress = Math.clamp(this.mqm$getMiningProgress() + this.calcEntityBreakingDelta(miner), 0, 1);
		this.getDataTracker().set(MINING_PROGRESS, (byte) (Byte.MIN_VALUE + (newProgress * BYTE_RANGE)));
	}

	@Override
	public void mqm$addMiner(ServerPlayerEntity miner) {
		this.miners.add(miner);
	}

	@Override
	public void mqm$stopMining(ServerPlayerEntity miner) {
		if(this.miners.remove(miner) && this.miners.isEmpty()) {
			this.mqm$resetMiningProgress();
		}
	}

	@Unique private float calcEntityBreakingDelta(PlayerEntity player) {
		float hardness = this.getIceHardness();

		int divisor;
		// We use STONE instead of ICE here to make it slower without a pickaxe.
		if(!player.canHarvest(Blocks.STONE.getDefaultState()))
			divisor = 100;
		else
			divisor = 30;

		// We use STONE instead of ICE here so that pickaxes will benefit from their tier.
		return player.getBlockBreakingSpeed(Blocks.STONE.getDefaultState()) / hardness / divisor;

//		return Blocks.ICE.getDefaultState().calcBlockBreakingDelta(player, this.getWorld(), this.getBlockPos());
	}

	@Unique
	protected float getIceHardness() {
		return Blocks.ICE.getHardness();
	}

	@Override
	public void mqm$resetMiningProgress() {
		this.miners.clear();
		this.getDataTracker().set(MINING_PROGRESS, Byte.MIN_VALUE);
	}

	@Override
	public float mqm$getMiningProgress() {
		return (((int) this.getDataTracker().get(MINING_PROGRESS)) - Byte.MIN_VALUE) / BYTE_RANGE;
	}

	@Inject(method = "damage", at = @At("HEAD"), cancellable = true)
	private void shatterOnDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if(!this.isInvulnerableTo(source) && this.mqm$shatter(source, amount))
			cir.setReturnValue(true);
	}

	@Inject(method = "getDimensions", at = @At("RETURN"), cancellable = true)
	private void useIceHitbox(EntityPose pose, CallbackInfoReturnable<EntityDimensions> cir) {
		if(this.mqm$isEncased()) {
			cir.setReturnValue(this.modifyDimensions(cir.getReturnValue()));
		}
	}

	@Unique protected float getIceHorizontalInflation() {
		return 0.3F;
	}
	@Unique protected float getIceVerticalInflation() {
		return 0.25F;
	}
	@Unique protected EntityDimensions modifyDimensions(EntityDimensions original) {
		float originalWidth = original.width(), originalHeight = original.height();
		float desiredWidth = originalWidth + this.getIceHorizontalInflation();
		float desiredHeight = originalHeight + this.getIceVerticalInflation();

		return original
				.scaled(desiredWidth / originalWidth, desiredHeight / originalHeight)
				.withEyeHeight(original.eyeHeight());
	}

	// MUST REFER TO onTrackedDataSet WITH THE EXPLICIT SIGNATURE LIKE THIS
	// Here in the world of Yarn, there's only one method named onTrackedDataSet.
	// However, in MojMaps this is named onSyncedDataUpdated, and shares this name with another method which Yarn names
	// onDataTrackerUpdate. In a Sinytra Connector environment, these methods go back to having the same name, which
	// causes things to break!!
	@Inject(method = "onTrackedDataSet(Lnet/minecraft/entity/data/TrackedData;)V", at = @At("TAIL"))
	private void recalculateDimensionsOnEncasementChange(TrackedData<?> data, CallbackInfo ci) {
		if(ENCASEMENT_POS.equals(data)) {
			this.calculateDimensions();
			this.setVelocity(Vec3d.ZERO);

			Vector3f pos = this.getDataTracker().get(ENCASEMENT_POS);
			if(!Float.isNaN(pos.x)) {
				this.refreshPositionAndAngles(pos.x, pos.y, pos.z, this.getYaw(), this.getPitch());
				this.onEncased();
			}
		}
	}

	@Unique protected void onEncased() {
		this.airSuspendTime = 0;
	}

	@WrapMethod(method = "isInvulnerableTo")
	private boolean ignoreSomeDamageTypesIfFrozen(DamageSource damageSource, Operation<Boolean> original) {
		return original.call(damageSource) || (
				this.mqm$isEncased()
				&& damageSource.isIn(MQMTags.ENCASED_ENTITIES_IGNORE)
				&& !damageSource.isIn(MQMTags.MULTIPLIES_SHATTER_DAMAGE)
		);
	}

	@Inject(method = "baseTick", at = @At("HEAD"))
	private void tickMining(CallbackInfo ci) {
		if(!this.miners.isEmpty()) {
			if(!this.mqm$canMine()) {
				this.mqm$resetMiningProgress();
				return;
			}

			for(Iterator<ServerPlayerEntity> iterator = this.miners.iterator(); iterator.hasNext(); ) {
				ServerPlayerEntity miner = iterator.next();
				this.mqm$updateMiningProgress(miner);

				if(!miner.canInteractWithEntity((Entity) (Object) this, 2)) {
					iterator.remove();
					continue;
				}

				if (this.mqm$getMiningProgress() >= 1) {
					this.mqm$shatter(
							miner.getDamageSources().playerAttack(miner),
							this.calculateShatterDamage(null, 0)
					);
					return;
				}
			}

			if(this.miners.isEmpty()) this.mqm$resetMiningProgress();
		}
	}

	@Unique private static final String IS_ENCASED_KEY = "Mqm_IsEncasedInIce";

	@Inject(
			method = "writeNbt",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/Entity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V"
			)
	)
	private void writeFrozenStatusToNbt(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
		nbt.putBoolean(IS_ENCASED_KEY, this.mqm$isEncased());
	}

	@Inject(
			method = "readNbt",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/Entity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V"
			)
	)
	private void readFrozenStatusFromNbt(NbtCompound nbt, CallbackInfo ci) {
		if(nbt.getBoolean(IS_ENCASED_KEY)) {
			this.getDataTracker().set(ENCASEMENT_POS, this.getPos().toVector3f());
		}
	}

	@ModifyReturnValue(method = {"canMoveVoluntarily", "couldAcceptPassenger", "canAddPassenger"}, at = @At("RETURN"))
	private boolean encasedCannotMoveOrAcceptPassengers(boolean original) {
		return original && !this.mqm$isEncased();
	}
}
