package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.util.Squashable;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@WrapOperation(method = "onEntityDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;onDamaged(Lnet/minecraft/entity/damage/DamageSource;)V"))
	private void squashFromSquashingDamage(Entity instance, DamageSource damageSource, Operation<Void> original) {
		if(instance instanceof LivingEntity livingInstance && damageSource.isIn(FLATTENS_ENTITIES_TAG))
			((Squashable) livingInstance).mqm$squash();
		original.call(instance, damageSource);
	}

	@Unique
	private static final TagKey<DamageType> FLATTENS_ENTITIES_TAG =
			TagKey.of(RegistryKeys.DAMAGE_TYPE, MarioQuaMarioContent.makeResID("flattens_entities"));
}
