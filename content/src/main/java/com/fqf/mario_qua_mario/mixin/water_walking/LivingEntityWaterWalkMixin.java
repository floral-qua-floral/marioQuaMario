package com.fqf.mario_qua_mario.mixin.water_walking;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityWaterWalkMixin extends Entity {
	public LivingEntityWaterWalkMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	@Unique protected boolean canWalkOnFluidHook(FluidState state) {
		return false;
	}

	@Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
	private void allowMiniWalkOnWater(FluidState state, CallbackInfoReturnable<Boolean> cir) {
		if(this.canWalkOnFluidHook(state)) cir.setReturnValue(true);
	}
}
