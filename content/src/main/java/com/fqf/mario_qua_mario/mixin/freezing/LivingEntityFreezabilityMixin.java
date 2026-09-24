package com.fqf.mario_qua_mario.mixin.freezing;

import com.fqf.mario_qua_mario.freezing.IceFlowerUtil;
import com.fqf.mario_qua_mario.util.MQMTags;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityFreezabilityMixin extends EntityFreezabilityMixin {
	@SuppressWarnings("BooleanMethodIsAlwaysInverted")
	@Shadow public abstract boolean isDead();
	@Shadow public abstract boolean damage(DamageSource source, float amount);

	@Shadow public abstract float getHealth();
	@Shadow public abstract float getMaxHealth();

	@Shadow private @Nullable DamageSource lastDamageSource;
	@Shadow protected float lastDamageTaken;

	@Shadow public abstract void playSound(@Nullable SoundEvent sound);
	@Shadow protected abstract @Nullable SoundEvent getDeathSound();

	@Shadow public abstract boolean canFreeze();

	@Shadow public abstract EntityDimensions getDimensions(EntityPose pose);

	@Shadow public abstract void onDeath(DamageSource damageSource);

	@Shadow public int hurtTime;
	@Shadow public int deathTime;

	@Shadow public abstract void setHealth(float health);

	@Shadow protected abstract void updatePostDeath();

	@Unique private float encasedTime;
	@Unique private boolean hasRumbled;
	@Unique private boolean canPermaFreezeEarly;

	@Override
	public boolean mqm$canBeEncased() {
		if(!this.mqm$isEncased() && !this.isDead() && !this.getType().isIn(MQMTags.CANNOT_ENCASE_IN_ICE)) {
			EntityDimensions dimensionsIfFrozen = this.getDimensions(this.getPose());
			return dimensionsIfFrozen.width() < 6 && dimensionsIfFrozen.height() < 8;
		}
		return false;
	}

	@Unique protected boolean isFatallyFrozen() {
		return Float.isInfinite(this.encasedTime);
	}

	@Override
	public boolean mqm$tickEncased() {
		if(!this.mqm$isEncased()) return true;

		if(!this.isFatallyFrozen() && this.encasedTime < 10) {
			if(!this.hasRumbled) {
				this.hasRumbled = true;
				this.rumble();
			}

			if(!this.getWorld().isClient && this.encasedTime <= 0 && this.mqm$thaw())
				return true;
		}

		if(this.isTouchingWater() || this.noClip) this.encasedTime -= 0.1F;
		else if(this.isOnGround()) this.encasedTime -= 1;

		boolean floatingUp = this.isTouchingWater() && this.getVelocity().y > 0;
		super.mqm$tickEncased();
		if(floatingUp && this.verticalCollision)
			this.mqm$thaw();

		return false;
	}

	@Override
	public boolean mqm$iceFlowerHit(@NotNull Entity source, @NotNull Entity attacker) {
		return
				this.mqm$canBeEncased()
				&& this.applyPermaFreezableDamage(source, attacker)
				&& !this.getType().isIn(MQMTags.DODGES_MARIO_FIREBALL)
				&& this.mqm$encase();
	}

	@Unique private boolean applyPermaFreezableDamage(Entity source, Entity attacker) {
		boolean damaged;
		try {
			this.canPermaFreezeEarly = true;
			damaged = this.damage(new DamageSource(
					source.getWorld().getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(IceFlowerUtil.ICEBALL_DAMAGE_TYPE),
					source, attacker
			), 2);
		}
		finally {
			this.canPermaFreezeEarly = false;
		}
		return damaged;
	}

	@Override
	public boolean mqm$encase() {
		if(super.mqm$encase()) {
			float healthBeforeIceball = this.getHealth();
			if(!this.isFatallyFrozen()) {
				if(this.lastDamageSource != null && this.lastDamageSource.isOf(IceFlowerUtil.ICEBALL_DAMAGE_TYPE))
					healthBeforeIceball += this.lastDamageTaken; // god this is jank :(

				this.encasedTime = this.getFreezeDuration(healthBeforeIceball);
			}
			this.hasRumbled = false;

			return true;
		}
		return false;
	}

	@Override
	public boolean mqm$attemptFatalFreeze(DamageSource source) {
		if(this.mqm$isEncased() || this.canPermaFreezeEarly) {
			if(!this.isFatallyFrozen()) {
				this.playSound(this.getDeathSound());
				this.onDeath(source);
			}
			this.encasedTime = Float.POSITIVE_INFINITY;
			return true;
		}
		return false;
	}

	@Override
	public boolean mqm$thaw() {
		if(super.mqm$thaw()) {
			if(this.isFatallyFrozen()) {
				this.setHealth(0);
				this.deathTime = 8000;
				this.updatePostDeath();
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean mqm$shatter(DamageSource source, float amount) {
		if(this.isFatallyFrozen()) {
			if(this.mqm$thaw()) {
				this.getWorld().playSound(
						null, this.getX(), this.getY(), this.getZ(),
						SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.BLOCKS,
						1, 0.8F
				);
				return true;
			}
			return false;
		}
		else if(super.mqm$shatter(source, amount)) {
			this.encasedTime = 0;
			return true;
		}
		return false;
	}

	@Override
	protected float getIceHardness() {
		return super.getIceHardness() * Math.max(1, this.getMaxHealth() / 8);
	}

	@Override
	public boolean mqm$isShaking() {
		return super.mqm$isShaking() || (!this.isFatallyFrozen() && this.encasedTime < 10 && this.encasedTime > -1);
	}

	@Override
	protected void onEncased() {
		super.onEncased();
		if(this.getWorld().isClient) {
			this.encasedTime = this.getFreezeDuration(this.getHealth());
			this.hurtTime = 0;
			this.deathTime = 0;
		}
	}

	@Inject(method = "applyDamage", at = @At("HEAD"))
	private void shatterOnApplyingDamage(DamageSource source, float amount, CallbackInfo ci) {
		if(!this.isInvulnerableTo(source)) this.mqm$shatter(source, amount);
	}

	@ModifyExpressionValue(
			method = "getDimensions",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/entity/LivingEntity;getBaseDimensions(Lnet/minecraft/entity/EntityPose;)Lnet/minecraft/entity/EntityDimensions;"
			)
	)
	private EntityDimensions inflateHitboxForIce(EntityDimensions original) {
		if(this.mqm$isEncased()) return this.modifyDimensions(original);
		return original;
	}

	@Unique protected float getFreezeDuration(float health) {
		return Math.max(3, 225 - health * 10) + this.getFrozenTicks();
	}

	@Inject(method = "updateLimbs(Z)V", at = @At("HEAD"), cancellable = true)
	private void preventUpdateLimbs(boolean flutter, CallbackInfo ci) {
		if(this.mqm$isEncased()) ci.cancel();
	}

	@Inject(method = "updateLimbs(F)V", at = @At("HEAD"), cancellable = true)
	private void preventUpdateLimbs(float posDelta, CallbackInfo ci) {
		if(this.mqm$isEncased()) ci.cancel();
	}

	@ModifyReturnValue(method = "isPushable", at = @At("RETURN"))
	private boolean preventBeingPushed(boolean original) {
		return original && !this.mqm$isEncased();
	}

	@Unique private static final String ENCASED_TIME = "Mqm_EncasedInIceTime";

	@Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
	private void writeEscapingVariables(NbtCompound nbt, CallbackInfo ci) {
		if(this.mqm$isEncased()) nbt.putFloat(ENCASED_TIME, this.encasedTime);
	}

	@Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
	private void readEscapingVariables(NbtCompound nbt, CallbackInfo ci) {
		this.encasedTime = nbt.getFloat(ENCASED_TIME);
	}
}
