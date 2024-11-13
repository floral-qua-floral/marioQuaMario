package com.floralquafloral.mixin;

import com.floralquafloral.bumping.BumpManager;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockRenderType;
import net.minecraft.client.render.chunk.SectionBuilder;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SectionBuilder.class)
public class SectionBuilderMixin {
	@ModifyExpressionValue(method = "build", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;getRenderType()Lnet/minecraft/block/BlockRenderType;"))
	private BlockRenderType preventRender(BlockRenderType original, @Local(ordinal = 2) BlockPos pos) {
		if(BumpManager.HIDDEN_BLOCKS.contains(pos)) return BlockRenderType.INVISIBLE;
		return original;
	}

//	@ModifyExpressionValue(method = "build", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayers;getBlockLayer(Lnet/minecraft/block/BlockState;)Lnet/minecraft/client/render/RenderLayer;"))
//	private RenderLayer gooble(RenderLayer original, @Local(ordinal = 2) BlockPos pos) {
//		return RenderLayer.getSolid();
//	}
}
