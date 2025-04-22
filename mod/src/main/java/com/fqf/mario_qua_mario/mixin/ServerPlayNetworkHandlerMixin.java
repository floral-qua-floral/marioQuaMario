package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.util.MarioGamerules;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
	@WrapOperation(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isInTeleportationState()Z"))
	private boolean isTeleportingOrCharacter(ServerPlayerEntity instance, Operation<Boolean> original) {
		return (instance.getServerWorld().getGameRules().getBoolean(MarioGamerules.DISABLE_CHARACTER_MOVEMENT_CHECK) && instance.mqm$getIMarioData().isEnabled()) || original.call(instance);
	}
}
