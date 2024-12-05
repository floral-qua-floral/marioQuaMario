package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.mariodata.MarioOtherClientData;
import com.fqf.mario_qua_mario.mariodata.injections.MarioOtherClientDataHolder;
import com.fqf.mario_qua_mario.mariodata.MarioPlayerData;
import com.fqf.mario_qua_mario.mixin.PlayerEntityMarioDataMixin;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OtherClientPlayerEntity.class)
public class OtherClientPlayerEntityMarioDataMixin extends PlayerEntityMarioDataMixin implements MarioOtherClientDataHolder {
	@Unique private MarioOtherClientData marioData;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void constructorHook(ClientWorld clientWorld, GameProfile gameProfile, CallbackInfo ci) {
		this.marioData = new MarioOtherClientData();
	}

	@Override
	public @NotNull MarioOtherClientData mqm$getMarioData() {
		return this.marioData;
	}

	@Override
	public void mqm$setMarioData(MarioPlayerData replacementData) {
		this.marioData = (MarioOtherClientData) replacementData;
	}
}
