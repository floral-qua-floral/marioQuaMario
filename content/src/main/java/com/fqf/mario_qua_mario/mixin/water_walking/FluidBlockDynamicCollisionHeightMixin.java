package com.fqf.mario_qua_mario.mixin.water_walking;

import com.fqf.mario_qua_mario.util.Powers;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidBlock.class)
public class FluidBlockDynamicCollisionHeightMixin {
	@Unique private static final VoxelShape TALLER = Block.createCuboidShape(
			0, 0, 0,
			16, 13.5, 16
	);

	@Inject(method = "getCollisionShape", at = @At("HEAD"))
	private void decideWhichCollisionShapeToUse(BlockState state, BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir, @Share("useTallerShape") LocalBooleanRef useTaller) {
		useTaller.set(context instanceof EntityShapeContextAccessor accessor && accessor.mqm$getEntity() instanceof PlayerEntity player && player.cfa$getCfaData().hasPower(Powers.TALLER_SOLID_WATER_HITBOX));
	}

	@ModifyExpressionValue(method = "getCollisionShape", at = @At(value = "FIELD", target = "Lnet/minecraft/block/FluidBlock;COLLISION_SHAPE:Lnet/minecraft/util/shape/VoxelShape;"))
	private VoxelShape useAlternateCollisionShape(VoxelShape original, @Share("useTallerShape") LocalBooleanRef useTaller) {
		return useTaller.get() ? TALLER : original;
	}
}
