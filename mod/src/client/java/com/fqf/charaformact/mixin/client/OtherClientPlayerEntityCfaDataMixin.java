package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.cfadata.CfaAppearanceData;
import com.fqf.charaformact.cfadata.CfaOtherClientData;
import com.fqf.charaformact.cfadata.injections.AdvCfaOtherClientDataHolder;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.injections.CfaClientDataHolder;
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
public class OtherClientPlayerEntityCfaDataMixin implements AdvCfaOtherClientDataHolder, CfaClientDataHolder {
	@Unique private CfaOtherClientData cfaData = new CfaOtherClientData((OtherClientPlayerEntity) (Object) this);

	@Override public CfaClientData cfa$getCfaClientData() {
		return this.cfa$getCfaData();
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void constructorHook(ClientWorld clientWorld, GameProfile gameProfile, CallbackInfo ci) {
		this.cfaData.initialApply();
	}

	@Override
	public @NotNull CfaOtherClientData cfa$getCfaData() {
		return this.cfaData;
	}

	@Override
	public @NotNull CfaAppearanceData<CfaOtherClientData> cfa$getModelData() {
		return this.cfaData.MODEL_DATA;
	}

}
