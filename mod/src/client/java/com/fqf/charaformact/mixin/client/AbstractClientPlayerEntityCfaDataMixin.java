package com.fqf.charaformact.mixin.client;

import com.fqf.charaformact.cfadata.CfaAnimationData;
import com.fqf.charaformact.cfadata.injections.AdvCfaAbstractClientDataHolder;
import com.fqf.charaformact_api.cfadata.injections.CfaClientDataHolder;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityCfaDataMixin implements AdvCfaAbstractClientDataHolder, CfaClientDataHolder {
	@Unique
	private final CfaAnimationData ANIMATION_DATA = new CfaAnimationData();

	@Override public @NotNull CfaAnimationData cfa$getAnimationData() {
		return this.ANIMATION_DATA;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void tickAnimationData(CallbackInfo ci) {
		this.cfa$getAnimationData().tick((AbstractClientPlayerEntity) (Object) this);
	}
}
