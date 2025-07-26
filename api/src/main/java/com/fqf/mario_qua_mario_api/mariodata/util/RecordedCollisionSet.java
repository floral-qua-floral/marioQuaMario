package com.fqf.mario_qua_mario_api.mariodata.util;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public interface RecordedCollisionSet extends Set<RecordedCollision> {
	boolean collidedOnAxis(Direction.Axis axis);
	Vec3d getPreCollisionVelocity();
	Vec3d getReflectedVelocity();
	Vec3d getHorizontallyReflectedVelocity();

	default boolean collidedHorizontally() {
		return this.collidedOnAxis(Direction.Axis.X) || this.collidedOnAxis(Direction.Axis.Z);
	}
	default boolean collidedVertically() {
		return this.collidedOnAxis(Direction.Axis.Y);
	}
}
