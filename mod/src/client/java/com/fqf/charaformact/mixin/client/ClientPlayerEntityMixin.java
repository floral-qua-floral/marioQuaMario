package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.cfadata.injections.AdvCfaMainClientDataHolder;
import com.fqf.charaformact.util.CfaPositionSettable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements AdvCfaMainClientDataHolder, CfaPositionSettable {
	@Shadow private double lastX, lastBaseY, lastZ;

	public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
		throw new AssertionError("Mixin constructor?!");
	}

	@Inject(method = "canSprint", at = @At("HEAD"), cancellable = true)
	private void preventSprinting(CallbackInfoReturnable<Boolean> cir) {
		if(this.cfa$getCfaData().doCustomTravel()) switch( this.cfa$getCfaData().getAction().SPRINTING_RULE) {
			case ALLOW:
				break;
			case IF_ALREADY_SPRINTING:
				if(isSprinting()) break;
			case PROHIBIT:
				cir.setReturnValue(false);
		}
	}

	@Inject(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tickMovement()V"))
	private void tickMovementClinger(CallbackInfo ci) {
		this.cfa$getCfaData().tickInputs();
	}

	@WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isTouchingWater()Z"))
	private boolean allowSprintingInPartialWater(ClientPlayerEntity instance, Operation<Boolean> original) {
		return !instance.cfa$getCfaData().isEnabled() && original.call(instance);
	}

	@WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
	private boolean moveFastWithItem(ClientPlayerEntity instance, Operation<Boolean> original) {
		return (!this.cfa$getCfaData().doCustomTravel() || instance.isOnGround()) && original.call(instance);
	}

	@Inject(method = "shouldSlowDown", at = @At("HEAD"), cancellable = true)
	private void preventSlowDown(CallbackInfoReturnable<Boolean> cir) {
		if(this.cfa$getCfaData().doCustomTravel()) cir.setReturnValue(false);
	}

	@Inject(method = "isInSneakingPose", at = @At("HEAD"), cancellable = true)
	private void preventSneakPose(CallbackInfoReturnable<Boolean> cir) {
		if(!this.cfa$getCfaData().isEnabled()) return;
		switch(this.cfa$getCfaData().getAction().SNEAKING_RULE) {
			case PROHIBIT -> cir.setReturnValue(false);
			case FORCE -> cir.setReturnValue(true);
		}
	}

	@Override
	public void cfa$setPos(Vec3d pos) {
		// will this fix it please
		this.lastX = pos.x;
		this.lastBaseY = pos.y;
		this.lastZ = pos.z;
	}

	@Inject(method = "sendMovementPackets", at = @At("RETURN"))
	private void sendHeldTransitionPackets(CallbackInfo ci) {
		List<CustomPayload> heldPackets = this.cfa$getCfaData().HELD_TRANSITION_PACKETS;
		for(CustomPayload heldTransitionPacket : heldPackets) {
			ClientPlayNetworking.send(heldTransitionPacket);
		}
		heldPackets.clear();
	}
}
