package com.fqf.mario_qua_mario.mixin.block_collisions;

import com.fqf.mario_qua_mario.util.Powers;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SnowBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SnowBlock.class)
public abstract class SnowLayerSolidityMixin {
	@Shadow protected abstract VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context);

	@Inject(method = "getCollisionShape", at = @At(value = "RETURN"), cancellable = true)
	private void useOutlineShapeForMiniPlayers(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
		if(
				context instanceof EntityShapeContextAccessor accessor
				&& accessor.mqm$getEntity() instanceof PlayerEntity player
				&& player.cfa$getCfaData().hasPower(Powers.SNOW_SHOES)
		)
			cir.setReturnValue(this.getOutlineShape(state, world, pos, context));
	}
}
