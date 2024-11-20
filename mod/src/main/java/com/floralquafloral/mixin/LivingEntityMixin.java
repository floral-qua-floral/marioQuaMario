package com.floralquafloral.mixin;

import com.floralquafloral.MarioQuaMario;
import com.floralquafloral.mariodata.MarioData;
import com.floralquafloral.mariodata.MarioDataManager;
import com.floralquafloral.registries.stomp.ParsedStomp;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
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

	@WrapOperation(method = "onDismounted", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;requestTeleportAndDismount(DDD)V"))
	private void marioDismountsInPlace(LivingEntity instance, double x, double y, double z, Operation<Void> original) {
		if((LivingEntity) (Object) this instanceof PlayerEntity mario && ((MarioData) MarioDataManager.getMarioData(mario)).isEnabled())
			original.call(instance, mario.getX(), Math.max(mario.getY(), instance.getY()), mario.getZ());
		else
			original.call(instance, x, y, z);
	}
}
