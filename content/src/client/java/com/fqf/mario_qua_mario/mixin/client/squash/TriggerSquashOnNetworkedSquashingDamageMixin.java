package com.fqf.mario_qua_mario.mixin.client.squash;

import com.fqf.mario_qua_mario.util.MQMTags;
import com.fqf.mario_qua_mario.util.Squashable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayNetworkHandler.class)
public class TriggerSquashOnNetworkedSquashingDamageMixin {
	@WrapOperation(method = "onEntityDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onDamaged(Lnet/minecraft/entity/damage/DamageSource;)V"))
	private void squashFromSquashingDamage(Entity instance, DamageSource damageSource, Operation<Void> original) {
		if(instance instanceof LivingEntity livingInstance && damageSource.isIn(MQMTags.FLATTENS_ENTITIES))
			((Squashable) livingInstance).cfa$squash();
		original.call(instance, damageSource);
	}
}
