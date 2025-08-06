package com.fqf.mario_qua_mario_content.util;

import com.fqf.mario_qua_mario_api.mariodata.IMarioReadableMotionData;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.wallbound.WallSlide;
import net.minecraft.util.math.Direction;

public class WallSlideableVars {
	private int collidingTicks;
	private Direction pushingDirection;

	public static boolean check(IMarioReadableMotionData data, boolean tick) {
		if (data.isServer()) return false;
		WallSlideableVars vars = data.getVars(WallSlideableVars.class);
		if (vars == null) {
			MarioQuaMarioContent.LOGGER.error("Trying to perform Wall Slide transition, but {} doesn't have WallSlideableVars!!",
					data.getActionID());
			return false;
		}

		if(tick) {
			Direction pushingDirection = data.getRecordedCollisions().getDirectionOfCollisionsWith((collision, block) ->
					WallSlide.canSlideDownBlock(collision.state()), false);
			if (pushingDirection != null && pushingDirection == vars.pushingDirection) {
				vars.collidingTicks++;
			} else {
				vars.pushingDirection = pushingDirection;
				vars.collidingTicks = 0;
			}
		}

		return vars.collidingTicks > 3;
	}
}
