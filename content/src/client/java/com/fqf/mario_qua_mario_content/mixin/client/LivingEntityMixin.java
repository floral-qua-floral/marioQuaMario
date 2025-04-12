package com.fqf.mario_qua_mario_content.mixin.client;

import com.fqf.mario_qua_mario_content.util.Squashable;
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

	@Inject(method = "tick", at = @At("HEAD"))
	private void resetSquashed(CallbackInfo ci) {
		if(!this.isDead()) this.squashed = false;
	}

	@Override
	public void mqm$squash() {
		this.squashed = true;
	}

	@Override
	public boolean mqm$isSquashed() {
		return this.squashed;
	}
}
