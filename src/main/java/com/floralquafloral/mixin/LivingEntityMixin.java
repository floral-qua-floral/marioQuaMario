package com.floralquafloral.mixin;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.registries.stomp.ParsedStomp;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	@Inject(method = "applyArmorToDamage", at = @At("RETURN"), cancellable = true)
	private void addStompPiercingDamage(DamageSource source, float amount, CallbackInfoReturnable<Float> cir) {
		if(source instanceof ParsedStomp.StompDamageSource stompDamageSource) {
			MarioQuaMario.LOGGER.info("Added piercing damage from Stomp: {}", stompDamageSource.getPiercing());
			cir.setReturnValue(cir.getReturnValue() + stompDamageSource.getPiercing());
		}
	}
}
