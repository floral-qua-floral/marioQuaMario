package com.fqf.mario_qua_mario.mixin.freezing;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEntity.class)
public abstract class MobEntityFreezabilityMixin extends LivingEntityFreezabilityMixin {
	@Inject(method = "playAmbientSound", at = @At("HEAD"), cancellable = true)
	private void noAmbientNoiseWhileFrozen(CallbackInfo ci) {
		if(this.mqm$isEncased()) ci.cancel();
	}

	@ModifyReturnValue(method = "cannotDespawn", at = @At("RETURN"))
	private boolean noDespawnWhilePermanentlyFrozen(boolean original) {
		return original || this.isFatallyFrozen();
	}
}
