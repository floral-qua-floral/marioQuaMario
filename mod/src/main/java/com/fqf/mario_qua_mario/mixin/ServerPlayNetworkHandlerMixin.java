package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.MarioGamerules;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
	@Shadow public ServerPlayerEntity player;

	@WrapOperation(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isInTeleportationState()Z"))
	private boolean isTeleportingOrCharacter(ServerPlayerEntity instance, Operation<Boolean> original) {
		return (instance.getServerWorld().getGameRules().getBoolean(MarioGamerules.DISABLE_CHARACTER_MOVEMENT_CHECK) && instance.mqm$getIMarioData().isEnabled()) || original.call(instance);
	}

	@WrapMethod(method = "requestTeleport(DDDFFLjava/util/Set;)V")
	private void requestTeleportHook(double x, double y, double z, float yaw, float pitch, Set<PositionFlag> flags, Operation<Void> original) {
		if(!this.player.mqm$getMarioData().cancelNextRequestTeleportPacket) {
			original.call(x, y, z, yaw, pitch, flags);
		}
	}
}
