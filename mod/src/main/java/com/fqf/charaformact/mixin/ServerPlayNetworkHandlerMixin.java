package com.fqf.charaformact.mixin;

import com.fqf.charaformact.util.CfaGamerules;
import com.fqf.charaformact.util.CfaPositionSettable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin implements CfaPositionSettable {
	@Shadow public ServerPlayerEntity player;

	@Shadow private double lastTickX, lastTickY, lastTickZ;

	@Shadow private double updatedX, updatedY, updatedZ;

	@WrapOperation(
			method = "onPlayerMove",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/network/ServerPlayerEntity;isInTeleportationState()Z"
			)
	)
	private boolean isTeleportingOrCharacter(ServerPlayerEntity instance, Operation<Boolean> original) {
		return (instance.getServerWorld().getGameRules().getBoolean(CfaGamerules.DISABLE_CHARACTER_MOVEMENT_CHECK) && instance.cfa$getCfaData().isEnabled()) || original.call(instance);
	}

	@Unique private boolean lastTickWasOnGround;
	@Inject(
			method = "syncWithPlayerPosition",
			at = @At("HEAD")
	)
	private void storeLastTickWasOnGround(CallbackInfo ci) {
		this.lastTickWasOnGround = this.player.isOnGround();
	}
	@Inject(
			method = "tick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/network/ServerPlayerEntity;updatePositionAndAngles(DDDFF)V",
					shift = At.Shift.AFTER
			)
	)
	private void restoreOnGroundStatusThatActuallyMatchesThePositionWeJustSetOhMyGod(CallbackInfo ci) {
		// This can update the player's position to be in midair, without resetting their isOnGround status.
		// As a result, it can cause adjustMovementForSneaking to fire even while the player isn't actually close
		// enough to the ground to detect it, which causes adjustMovementForSneaking to completely zero out the input
		// motion.
		// I think the only reason this doesn't cause "moved wrongly" errors in vanilla is that it requires the player
		// to be falling relatively fast while also sneaking while also traveling horizontally at a high enough speed
		// for the discrepancy to trip the "moved wrongly" check.
		this.player.setOnGround(this.lastTickWasOnGround);
	}

	@Override
	public void cfa$setPos(Vec3d pos) {
		// for the love of god please
		this.lastTickX = pos.x;
		this.lastTickY = pos.y;
		this.lastTickZ = pos.z;
		this.updatedX = pos.x;
		this.updatedY = pos.y;
		this.updatedZ = pos.z;
	}
}
