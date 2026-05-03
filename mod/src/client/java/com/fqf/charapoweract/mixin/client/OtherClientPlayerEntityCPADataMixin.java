package com.fqf.charapoweract.mixin.client;

import com.fqf.charapoweract.cpadata.CPAOtherClientData;
import com.fqf.charapoweract.cpadata.injections.AdvCPAOtherClientDataHolder;
import com.fqf.charapoweract_api.cpadata.ICPAClientData;
import com.fqf.charapoweract.cpadata.CPAPlayerData;
import com.fqf.charapoweract_api.cpadata.injections.ICPAClientDataHolder;
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
public class OtherClientPlayerEntityCPADataMixin implements AdvCPAOtherClientDataHolder, ICPAClientDataHolder {
	@Unique private CPAOtherClientData cpaData = new CPAOtherClientData((OtherClientPlayerEntity) (Object) this);

	@Override public ICPAClientData cpa$getICPAClientData() {
		return this.cpa$getCPAData();
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void constructorHook(ClientWorld clientWorld, GameProfile gameProfile, CallbackInfo ci) {
		this.cpa$setCPAData(this.cpaData);
	}

	@Override
	public @NotNull CPAOtherClientData cpa$getCPAData() {
		return this.cpaData;
	}

	@Override
	public void cpa$setCPAData(CPAPlayerData replacementData) {
		this.cpaData = (CPAOtherClientData) replacementData;
		replacementData.initialApply();
	}
}
