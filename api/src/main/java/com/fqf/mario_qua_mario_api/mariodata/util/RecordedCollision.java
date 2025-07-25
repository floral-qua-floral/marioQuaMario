package com.fqf.mario_qua_mario_api.mariodata.util;

import com.fqf.mario_qua_mario_api.interfaces.BapResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public record RecordedCollision(BlockPos pos, Direction direction, Vec3d preCollisionMotion, @Nullable BapResult bapResult) {
	public Vec3d getReflectedMotion() {
		Direction.Axis flipOnAxis = this.direction.getAxis();
		return this.preCollisionMotion.withAxis(flipOnAxis, this.preCollisionMotion.getComponentAlongAxis(flipOnAxis));
	}
}
