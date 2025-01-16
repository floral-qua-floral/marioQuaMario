package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.mariodata.MarioAnimationData;
import com.fqf.mario_qua_mario.mariodata.MarioMainClientData;
import com.fqf.mario_qua_mario.mariodata.injections.MarioMainClientDataHolder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements MarioMainClientDataHolder {
	public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
		super(world, profile);
		throw new AssertionError("Mixin constructor?!");
	}

	@Inject(method = "canSprint", at = @At("HEAD"), cancellable = true)
	private void preventSprinting(CallbackInfoReturnable<Boolean> cir) {
		if(mqm$getMarioData().doMarioTravel()) switch( mqm$getMarioData().getAction().SPRINTING_RULE) {
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
		mqm$getMarioData().tickInputs();
	}

	@WrapOperation(method = "tickMovement", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
	private boolean moveFastWithItem(ClientPlayerEntity instance, Operation<Boolean> original) {
		return (!mqm$getMarioData().doMarioTravel() || instance.isOnGround()) && original.call(instance);
	}

	@Override
	public @NotNull MarioAnimationData mqm$getAnimationData() {
		return super.mqm$getAnimationData();
	}

	@Override
	public @NotNull MarioMainClientData mqm$getMarioData() {
		throw new AssertionError("?!");
	}

	@Inject(method = "shouldSlowDown", at = @At("HEAD"), cancellable = true)
	private void preventSlowDown(CallbackInfoReturnable<Boolean> cir) {
		if(mqm$getMarioData().doMarioTravel()) cir.setReturnValue(false);
	}

	@Inject(method = "isInSneakingPose", at = @At("HEAD"), cancellable = true)
	private void preventSneakPose(CallbackInfoReturnable<Boolean> cir) {
		switch(mqm$getMarioData().getAction().SNEAKING_RULE) {
			case PROHIBIT -> cir.setReturnValue(false);
			case FORCE -> cir.setReturnValue(true);
		}
	}
}
