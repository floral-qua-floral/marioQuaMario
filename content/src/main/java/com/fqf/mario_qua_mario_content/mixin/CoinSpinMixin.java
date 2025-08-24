package com.fqf.mario_qua_mario_content.mixin;

import com.fqf.mario_qua_mario_content.item.ModItems;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public abstract class CoinSpinMixin {
	@Shadow public abstract ItemStack getStack();

	@Unique private static final float COIN_ROTATION_FACTOR = -6.75F;

	@ModifyReturnValue(method = "getRotation", at = @At("RETURN"))
	private float multiplyRotation(float original) {
		if(this.getStack().isOf(ModItems.COIN)) original *= COIN_ROTATION_FACTOR;
		return original;
	}
}
