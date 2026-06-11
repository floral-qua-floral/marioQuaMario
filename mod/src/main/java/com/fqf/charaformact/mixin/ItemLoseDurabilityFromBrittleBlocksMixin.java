package com.fqf.charaformact.mixin;

import com.fqf.charaformact.bapping.BlockBappingUtil;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public class ItemLoseDurabilityFromBrittleBlocksMixin {
	@WrapOperation(method = "postMine", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getHardness(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;)F"))
	private float alwaysGetVanillaHardness(BlockState instance, BlockView blockView, BlockPos blockPos, Operation<Float> original) {
		return BlockBappingUtil.getVanillaHardnessForMixin(instance, blockView, blockPos, original);
	}
}
