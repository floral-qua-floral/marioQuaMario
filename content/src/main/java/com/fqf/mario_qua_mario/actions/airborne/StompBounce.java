package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.PiecemealPlayermodelAnimation;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.fqf.charaformact_api.util.StatCategory.JUMP_VELOCITY;
import static com.fqf.charaformact_api.util.StatCategory.COLLISION_ATTACK;

public class StompBounce extends Jump implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("stomp");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @Nullable AnimationDefinition getAnimation() {
		return DoubleJump.ANIMATION;
	}

	//	@Override public @Nullable PiecemealPlayermodelAnimation getOldAnimation(AnimationHelper helper) {
//		return DoubleJump.ANIMATION;
//	}

	public static final CfaStat BOUNCE_VEL = new CfaStat(1.15, COLLISION_ATTACK, JUMP_VELOCITY);

	@Override
	protected double getJumpCapThreshold() {
		return 0.65;
	}
}
