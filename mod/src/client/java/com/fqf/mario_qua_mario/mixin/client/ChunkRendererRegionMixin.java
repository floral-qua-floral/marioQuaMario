package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.bapping.BlockBappingUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.chunk.ChunkRendererRegion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkRendererRegion.class)
public class ChunkRendererRegionMixin {
	@Shadow @Final protected World world;

	@Inject(method = "getBlockState", at = @At("RETURN"), cancellable = true)
	private void hideHiddenBlocks(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
//		cir.setReturnValue(Blocks.AIR.getDefaultState());
		BlockBappingUtil.conditionallyHideBlockPos(this.world, pos, cir);
	}
}
