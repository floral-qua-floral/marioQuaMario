package com.fqf.mario_qua_mario_content.util;

import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario_content.actions.wallbound.WallSlide;
import net.minecraft.util.math.Direction;

public class MarioVars {
	public int canDoubleJumpTicks = 0;
	public int canTripleJumpTicks = 0;
	public double pSpeed = 0; // Range 0-1

	public double stompGuardMinHeight;
	public int stompGuardRemainingTicks;

	private long lastWallSlideTick = Long.MIN_VALUE;
	private Direction lastWallSlideDirection;
	private int wallSlidingTicks;

	public static MarioVars get(IMarioData data) {
		return data.getVars(MarioVars.class);
	}

	public static boolean checkWallSlide(IMarioReadableMotionData data) {
		if(data.getYVel() > 0) return false;

		long worldTime = data.getMario().getWorld().getTime();
		MarioVars vars = data.getVars(MarioVars.class);

		processing: {
			if(vars.lastWallSlideTick == worldTime) {
				break processing;
			}
			else if(vars.lastWallSlideTick != worldTime - 1) {
				vars.wallSlidingTicks = 0;
				vars.lastWallSlideDirection = null;
			}

			vars.lastWallSlideTick = worldTime;

			Direction pushingDirection = data.getRecordedCollisions().getDirectionOfCollisionsWith((collision, block) ->
					WallSlide.canSlideDownBlock(collision.state()), false);
			if (pushingDirection != null && pushingDirection == vars.lastWallSlideDirection) {
				vars.wallSlidingTicks++;
			} else {
				vars.lastWallSlideDirection = pushingDirection;
				vars.wallSlidingTicks = 0;
			}
		}

		return vars.wallSlidingTicks >= 2;
	}
}
