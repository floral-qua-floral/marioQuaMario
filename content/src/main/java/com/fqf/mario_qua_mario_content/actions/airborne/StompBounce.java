package com.fqf.mario_qua_mario_content.actions.airborne;

import com.fqf.charapoweract_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charapoweract_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.charapoweract_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.fqf.charapoweract_api.util.StatCategory.JUMP_VELOCITY;
import static com.fqf.charapoweract_api.util.StatCategory.COLLISION_ATTACK;

public class StompBounce extends Jump implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("stomp");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return DoubleJump.ANIMATION;
	}

	public static final CharaStat BOUNCE_VEL = new CharaStat(1.15, COLLISION_ATTACK, JUMP_VELOCITY);

	@Override
	protected double getJumpCapThreshold() {
		return 0.65;
	}
}
