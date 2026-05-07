package com.fqf.charaformact.mixin.compat;

import com.tom.cpm.shared.network.NetHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandler.class)
public class CPMNetHandlerMixin<RL, P, NET> {
	@Inject(method = "onJoin", at = @At("TAIL"), remap = false)
	private void activateServerCfaData(P player, CallbackInfo ci) {
		((ServerPlayerEntity) player).cfa$getCfaData().initialApply();
	}
}
