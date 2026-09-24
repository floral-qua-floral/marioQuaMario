package com.fqf.mario_qua_mario.mixin.client.freezing;

import com.fqf.mario_qua_mario.freezing.IceFlowerUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientSuppressFrozenEntityTickingMixin {
	@Shadow protected abstract void tickPassenger(Entity vehicle, Entity passenger);

	@Inject(
			method = "tickEntity",
			at = @At("HEAD"),
			cancellable = true
	)
	private void suppressTicking(Entity entity, CallbackInfo ci) {
		IceFlowerUtil.doEntityTickInjection(entity, ci, this::tickPassenger);
	}
}
