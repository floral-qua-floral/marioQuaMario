package com.fqf.mario_qua_mario.util;

import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaReadableMotionData;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.wallbound.WallSlide;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Direction;

import java.util.Objects;

public class MarioVars {
	public int canDoubleJumpTicks = 0;
	public int canTripleJumpTicks = 0;
	public double pSpeed = 0; // Range 0-1

	public double stompGuardMinHeight;
	public int stompGuardRemainingTicks;

	private long lastWallSlideTick = Long.MIN_VALUE;
	private Direction lastWallSlideDirection;
	private int wallSlidingTicks;

	public boolean hasUnarmedModifier;

	public static MarioVars get(CfaData data) {
		return data.retrieveStateData(MarioVars.class);
	}

	public static boolean checkWallSlide(CfaReadableMotionData data) {
		if(data.getYVel() > 0 || data.isNearGround(0.5) || data.getInputs().DUCK.isHeld()) return false;

		long worldTime = data.getPlayer().getWorld().getTime();
		MarioVars vars = data.retrieveStateData(MarioVars.class);

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
