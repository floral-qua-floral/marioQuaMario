package com.fqf.charapoweract.mixin.compat;

import com.fqf.charapoweract.CharaPowerAct;
import com.fqf.charapoweract.compat.required.CPMCompat;
import com.tom.cpm.CommonBase;
import com.tom.cpm.api.CPMApiManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommonBase.class)
public class CPMCommonBaseMixin {
	@Shadow(remap = false) public static CPMApiManager api;

	@Inject(method = "apiInit", at = @At("HEAD"), remap = false)
	private void manuallyRegisterMQMPluginIfAbsent(CallbackInfo ci) {
		if(!CPMCompat.isRegistered()) {
			CharaPowerAct.LOGGER.warn("MQM CPM plugin wasn't registered normally. Are we in Sinytra? Adding it manually...");
			api.register(new CPMCompat());
		}
	}
}
