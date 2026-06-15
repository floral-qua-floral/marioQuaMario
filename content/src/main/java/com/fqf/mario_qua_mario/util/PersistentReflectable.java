package com.fqf.mario_qua_mario.util;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public interface PersistentReflectable {
	boolean mqm$isInGround();
	Vec3d mqm$getGroundNormal();
	Direction mqm$getGroundStickFace();
	void mqm$dislodge();
}
