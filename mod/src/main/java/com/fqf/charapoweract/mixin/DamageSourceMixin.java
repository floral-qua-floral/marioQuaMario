package com.fqf.charapoweract.mixin;

import com.fqf.charapoweract.util.CollisionAttackDamageSource;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DamageSource.class)
public class DamageSourceMixin {
	@WrapOperation(method = "getDeathMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"))
	private ItemStack useStompWeapon(LivingEntity killed, Operation<ItemStack> original) {
		if((DamageSource) (Object) this instanceof CollisionAttackDamageSource collisionAttackDamageSource)
			return collisionAttackDamageSource.WEAPON_STACK;
		return original.call(killed);
	}
}
