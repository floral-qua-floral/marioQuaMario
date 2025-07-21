package com.fqf.mario_qua_mario.mixin;

import com.fqf.mario_qua_mario.bapping.BlockBappingUtil;
import com.fqf.mario_qua_mario.bapping.WorldBapsInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RedstoneView;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(RedstoneView.class)
public interface RedstoneViewMixin {
	@Inject(method = "isReceivingRedstonePower", at = @At("HEAD"), cancellable = true)
	private void forceReceiveRedstonePower(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
		if(this instanceof World world) {
			WorldBapsInfo worldBaps = BlockBappingUtil.getBapsInfoNullable(world);
			if(worldBaps != null && worldBaps.POWERED.contains(pos))
				cir.setReturnValue(true);
		}
	}

	@Inject(method = "getEmittedRedstonePower(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;)I", at = @At("HEAD"), cancellable = true)
	private void forceReceiveEmittedRedstonePower(BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> cir) {
		if(this instanceof World world) {
			WorldBapsInfo worldBaps = BlockBappingUtil.getBapsInfoNullable(world);
			if(worldBaps != null && worldBaps.POWERED.contains(pos.offset(direction.getOpposite())))
				cir.setReturnValue(1);
		}
	}
}
