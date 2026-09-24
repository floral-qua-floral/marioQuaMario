package com.fqf.mario_qua_mario.mixin.freezing;

import com.fqf.mario_qua_mario.freezing.IceFlowerUtil;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class SuppressFrozenEntityTickingMixin {
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
