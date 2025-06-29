package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.bapping.BlockBappingUtil;
import net.minecraft.block.AbstractBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin {
	@Inject(method = "getHardness", at = @At("HEAD"), cancellable = true)
	private void getBrittleHardness(BlockView world, BlockPos pos, CallbackInfoReturnable<Float> cir) {
		if(world instanceof World trueWorld) {
			if(BlockBappingUtil.getCertain(BlockBappingUtil.BRITTLE_BLOCK_POSITIONS, trueWorld).contains(pos)) {
				cir.setReturnValue(0F);
			}
		}
	}
}
