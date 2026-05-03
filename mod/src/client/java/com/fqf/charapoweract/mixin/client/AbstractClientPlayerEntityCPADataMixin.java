package com.fqf.charapoweract.mixin.client;

import com.fqf.charapoweract.cpadata.CPAAnimationData;
import com.fqf.charapoweract.cpadata.injections.AdvCPAAbstractClientDataHolder;
import com.fqf.charapoweract_api.cpadata.injections.ICPAClientDataHolder;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractClientPlayerEntity.class)
public class AbstractClientPlayerEntityCPADataMixin implements AdvCPAAbstractClientDataHolder, ICPAClientDataHolder {
	@Unique
	private final CPAAnimationData ANIMATION_DATA = new CPAAnimationData();

	@Override public @NotNull CPAAnimationData cpa$getAnimationData() {
		return this.ANIMATION_DATA;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void tickAnimationData(CallbackInfo ci) {
		this.cpa$getAnimationData().tick((AbstractClientPlayerEntity) (Object) this);
	}
}
