package com.fqf.mario_qua_mario.mixin;

import com.tom.cpm.shared.network.NetHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandler.class)
public class CPMNetHandlerMixin<RL, P, NET> {
	@Inject(method = "onJoin", at = @At("TAIL"), remap = false)
	private void activateServerMarioData(P player, CallbackInfo ci) {
		((ServerPlayerEntity) player).mqm$getMarioData().initialApply();
	}
}
