package com.fqf.mario_qua_mario.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record VoxelShapeWithBlockPos(@NotNull VoxelShape shape, @Nullable BlockPos pos) {
}
