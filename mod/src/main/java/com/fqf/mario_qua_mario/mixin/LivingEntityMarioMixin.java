package com.fqf.mario_qua_mario.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMarioMixin {
	@WrapWithCondition(method = "onDismounted", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;requestTeleportAndDismount(DDD)V"))
	private boolean teleportOnDismountCondition(LivingEntity instance, double x, double y, double z) {
//		if(instance instanceof ServerPlayerEntity mario) {
//			if(mario.mqm$getMarioData().isEnabled() && mario.mqm$getMarioData().cancelNextRequestTeleportPacket) {
//				mario.mqm$getMarioData().cancelNextRequestTeleportPacket = false;
//				mario.dismountVehicle();
//				return false;
//			}
//		}

		return true;
	}
}
