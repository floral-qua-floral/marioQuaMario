package com.floralquafloral.mixin;

import com.floralquafloral.registries.stomp.StompHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DamageSource.class)
public abstract class DamageSourceMixin {
	@Shadow public abstract boolean isIn(TagKey<DamageType> tag);

	@WrapOperation(method = "getDeathMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getMainHandStack()Lnet/minecraft/item/ItemStack;"))
	protected ItemStack uwu(LivingEntity instance, Operation<ItemStack> original) {
		if(isIn(StompHandler.USES_FEET_ITEM_TAG)) return instance.getEquippedStack(EquipmentSlot.FEET);
		if(isIn(StompHandler.USES_LEGS_ITEM_TAG)) return instance.getEquippedStack(EquipmentSlot.LEGS);
		return original.call(instance);
	}
}
