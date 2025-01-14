package com.fqf.mario_qua_mario.util;

import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class MarioVars {
	public int jumpLandingTime = 0;
	public int doubleJumpLandingTime = 0;

	public static MarioVars get(IMarioData data) {
		return data.getVars(MarioVars.class);
	}
}
