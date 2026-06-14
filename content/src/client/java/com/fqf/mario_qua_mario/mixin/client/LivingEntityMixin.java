package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.util.Squashable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Squashable {
	@Shadow public abstract boolean isDead();

	public LivingEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Unique private boolean squashed;
	@Unique private long squashStartTime, squashEndTime;
	@Unique private static final int SQUASH_DURATION = 15;

	@Inject(method = "tick", at = @At("HEAD"))
	private void resetSquashed(CallbackInfo ci) {
		if(!this.isDead() && this.getWorld().getTime() >= squashEndTime) this.squashed = false;
	}

	@Override
	public void cfa$squash() {
		this.squashed = true;
		this.squashStartTime = this.getWorld().getTime();
		this.squashEndTime = this.squashStartTime + SQUASH_DURATION;
	}

	@Override
	public boolean cfa$isSquashed() {
		return this.squashed;
	}

	@Override
	public float cfa$getSquashProgress(float tickDelta) {
		if(this.squashed) return (((float) (this.getWorld().getTime() - this.squashStartTime)) + tickDelta) / SQUASH_DURATION;
		return 0;
	}
}
