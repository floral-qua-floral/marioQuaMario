package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.MarioGamerules;
import com.fqf.mario_qua_mario.util.MarioPositionSettable;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin implements MarioPositionSettable {
	@Shadow public ServerPlayerEntity player;

	@Shadow private double lastTickX, lastTickY, lastTickZ;

	@Shadow private double updatedX, updatedY, updatedZ;

	@WrapOperation(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;isInTeleportationState()Z"))
	private boolean isTeleportingOrCharacter(ServerPlayerEntity instance, Operation<Boolean> original) {
		return (instance.getServerWorld().getGameRules().getBoolean(MarioGamerules.DISABLE_CHARACTER_MOVEMENT_CHECK) && instance.mqm$getIMarioData().isEnabled()) || original.call(instance);
	}

//	@Inject(method = "onPlayerMove", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Object;)V"))
//	private void uwu(PlayerMoveC2SPacket packet, CallbackInfo ci) {
//		MarioQuaMario.LOGGER.info("Moved wrongly. Information:\n\tLast tick: ({}, {}, {})\n\tPlayer position: ({}, {}, {})\n\tPacket target: ({}, {}, {})",
//				this.lastTickX, this.lastTickY, this.lastTickZ,
//				player.getX(), player.getY(), player.getZ(),
//				packet.getX(0), packet.getY(0), packet.getZ(0));
//	}


	@WrapMethod(method = "requestTeleport(DDDFFLjava/util/Set;)V")
	private void requestTeleportHook(double x, double y, double z, float yaw, float pitch, Set<PositionFlag> flags, Operation<Void> original) {
		if(!this.player.mqm$getMarioData().cancelNextRequestTeleportPacket) {
			original.call(x, y, z, yaw, pitch, flags);
		}
	}

	@Override
	public void mqm$setPos(Vec3d pos) {
		// for the love of god please
		this.lastTickX = pos.x;
		this.lastTickY = pos.y;
		this.lastTickZ = pos.z;
		this.updatedX = pos.x;
		this.updatedY = pos.y;
		this.updatedZ = pos.z;
	}
}
