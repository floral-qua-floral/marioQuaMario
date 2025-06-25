package com.fqf.mario_qua_mario.mixin.client;

import com.fqf.mario_qua_mario.bapping.BlockBappingUtil;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(LevelSlice.class)
public class SodiumLevelSliceMixin {
	@Shadow @Final private ClientWorld level;

	@Inject(method = "getBlockState(III)Lnet/minecraft/block/BlockState;", at = @At("RETURN"), cancellable = true)
	private void hideHiddenBlocks(int blockX, int blockY, int blockZ, CallbackInfoReturnable<BlockState> cir) {
		BlockBappingUtil.conditionallyHideBlockPos(this.level, new BlockPos(blockX, blockY, blockZ), cir);
	}
//	@Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
//	private static void hideBlocks(BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos otherPos, CallbackInfoReturnable<Boolean> cir) {
//		if(pos.getX() % 3 == 0) cir.setReturnValue(false);
//		else if(otherPos.getX() % 3 == 0) cir.setReturnValue(true);
//	}
}
