package com.floralquafloral.mixin;

import com.floralquafloral.MarioQuaMario;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
	public void interceptUnarmedAttack(CallbackInfoReturnable<Boolean> cir) {
		MarioQuaMario.LOGGER.info("doAttack mixin!");
//		cir.setReturnValue(false);
	}
}
