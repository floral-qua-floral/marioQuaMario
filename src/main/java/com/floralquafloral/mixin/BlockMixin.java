package com.floralquafloral.mixin;

import com.floralquafloral.bumping.BumpManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockMixin {
	/**
	 * this implementation is YUCKY i would strongly prefer something in the chunk building stuff or something.
	 * maybe in SectionBuilder? to make it think the block is transparent when building?
	 * as opposed to this dumb approach. >:(
	 * but i don't know how to do that. ;-;
	 */
	@Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
	private static void uwuuber(BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos otherPos, CallbackInfoReturnable<Boolean> cir) {
		if(BumpManager.HIDDEN_BLOCKS.contains(otherPos)) cir.setReturnValue(true);
	}
}
