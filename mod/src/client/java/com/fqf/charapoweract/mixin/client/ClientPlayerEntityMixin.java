package com.fqf.charapoweract.mixin.client;

import com.fqf.charapoweract.cpadata.CPAMainClientData;
import com.fqf.charapoweract.cpadata.injections.AdvCPAMainClientDataHolder;
import com.fqf.charapoweract.util.CPAPositionSettable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements AdvCPAMainClientDataHolder, CPAPositionSettable {
	@Shadow private double lastX, lastBaseY, lastZ;

	public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
		throw new AssertionError("Mixin constructor?!");
	}

	@Inject(method = "canSprint", at = @At("HEAD"), cancellable = true)
	private void preventSprinting(CallbackInfoReturnable<Boolean> cir) {
		if(this.cpa$getCPAData().doCustomTravel()) switch( this.cpa$getCPAData().getAction().SPRINTING_RULE) {
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
		this.cpa$getCPAData().tickInputs();
	}

	@WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isTouchingWater()Z"))
	private boolean allowSprintingInPartialWater(ClientPlayerEntity instance, Operation<Boolean> original) {
		return !instance.cpa$getCPAData().isEnabled() && original.call(instance);
	}

	@WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
	private boolean moveFastWithItem(ClientPlayerEntity instance, Operation<Boolean> original) {
		return (!this.cpa$getCPAData().doCustomTravel() || instance.isOnGround()) && original.call(instance);
	}

	@Override
	public @NotNull CPAMainClientData cpa$getCPAData() {
		throw new AssertionError("?!");
	}

	@Inject(method = "shouldSlowDown", at = @At("HEAD"), cancellable = true)
	private void preventSlowDown(CallbackInfoReturnable<Boolean> cir) {
		if(this.cpa$getCPAData().doCustomTravel()) cir.setReturnValue(false);
	}

	@Inject(method = "isInSneakingPose", at = @At("HEAD"), cancellable = true)
	private void preventSneakPose(CallbackInfoReturnable<Boolean> cir) {
		if(!this.cpa$getCPAData().isEnabled()) return;
		switch(this.cpa$getCPAData().getAction().SNEAKING_RULE) {
			case PROHIBIT -> cir.setReturnValue(false);
			case FORCE -> cir.setReturnValue(true);
		}
	}

	@Override
	public void cpa$setPos(Vec3d pos) {
		// will this fix it please
		this.lastX = pos.x;
		this.lastBaseY = pos.y;
		this.lastZ = pos.z;
	}

	@Inject(method = "sendMovementPackets", at = @At("RETURN"))
	private void sendHeldTransitionPackets(CallbackInfo ci) {
		List<CustomPayload> heldPackets = this.cpa$getCPAData().HELD_TRANSITION_PACKETS;
		for(CustomPayload heldTransitionPacket : heldPackets) {
			ClientPlayNetworking.send(heldTransitionPacket);
		}
		heldPackets.clear();
	}
}
