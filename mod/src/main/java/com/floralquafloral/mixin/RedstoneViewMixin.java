package com.floralquafloral.mixin;

import com.floralquafloral.bumping.BlockBumpHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RedstoneView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RedstoneView.class)
public interface RedstoneViewMixin {
	@Inject(method = "isReceivingRedstonePower", at = @At("HEAD"), cancellable = true)
	private void forceReceiveRedstonePower(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if(BlockBumpHandler.FORCED_SIGNALS.contains(pos)) cir.setReturnValue(true);
	}

	@Inject(method = "getEmittedRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", at = @At("HEAD"), cancellable = true)
	private void forceReceiveRedstonePowerBravo(BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> cir) {
		if(BlockBumpHandler.FORCED_SIGNALS.contains(pos.offset(direction.getOpposite())))
			cir.setReturnValue(1);
	}
}
