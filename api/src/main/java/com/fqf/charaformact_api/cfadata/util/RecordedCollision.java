package com.fqf.charaformact_api.cfadata.util;

import com.fqf.charaformact_api.interfaces.BapResult;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

public record RecordedCollision(BlockPos pos, BlockState state, Direction direction, @Nullable BapResult bapResult) {
}
