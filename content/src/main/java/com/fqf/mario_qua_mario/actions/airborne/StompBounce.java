package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.Easing;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.fqf.mario_qua_mario.util.StatCategory.JUMP_VELOCITY;
import static com.fqf.mario_qua_mario.util.StatCategory.STOMP;

public class StompBounce extends Jump implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("stomp");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return DoubleJump.ANIMATION;
	}

	public static final CharaStat BOUNCE_VEL = new CharaStat(1.15, STOMP, JUMP_VELOCITY);

	@Override
	protected double getJumpCapThreshold() {
		return 0.65;
	}
}
