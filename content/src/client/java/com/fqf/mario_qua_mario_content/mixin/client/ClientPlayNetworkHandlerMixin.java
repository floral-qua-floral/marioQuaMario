package com.fqf.mario_qua_mario_content.mixin.client;

import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.util.MQMContentTags;
import com.fqf.mario_qua_mario_content.util.Squashable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@WrapOperation(method = "onEntityDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onDamaged(Lnet/minecraft/entity/damage/DamageSource;)V"))
	private void squashFromSquashingDamage(Entity instance, DamageSource damageSource, Operation<Void> original) {
		if(instance instanceof LivingEntity livingInstance && damageSource.isIn(MQMContentTags.FLATTENS_ENTITIES))
			((Squashable) livingInstance).mqm$squash();
		original.call(instance, damageSource);
	}
}
