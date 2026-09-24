package com.fqf.mario_qua_mario.mixin.block_collisions;

import com.fqf.mario_qua_mario.util.Powers;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PowderSnowBlock.class)
public class PowderSnowWalkingMixin {
	@Inject(method = "canWalkOnPowderSnow", at = @At("HEAD"), cancellable = true)
	private static void powderSnowWalkingPower(Entity entity, CallbackInfoReturnable<Boolean> cir) {
		if(entity instanceof PlayerEntity player && player.cfa$getCfaData().hasPower(Powers.SNOW_SHOES))
			cir.setReturnValue(true);
	}
}
