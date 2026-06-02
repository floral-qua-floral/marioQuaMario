package com.fqf.mario_qua_mario.actions.aquatic;

import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class Swim extends Submerged {
	public static final Identifier ID = MarioQuaMario.makeID("swim");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override protected float getAnimationProgress(float animationTime) {
		return Math.min(animationTime / 2, 3);
	}

	public static final CfaStat SWIM_ACCEL = new CfaStat(0.4, SWIMMING, UP, ACCELERATION);
	public static final CfaStat SWIM_MAX_ASCENSION_SPEED = new CfaStat(0.45, SWIMMING, UP, SPEED);

	public static final TransitionDefinition SWIM = new TransitionDefinition(
			ID,
			data -> {
				ActionTimerVars vars = data.retrieveStateData(ActionTimerVars.class);
				return (vars == null || vars.actionTimer > 2) && data.getInputs().JUMP.isPressed();
			},
			EvaluatorEnvironment.CLIENT_ONLY,
			data -> {
				data.setYVel(Math.min(SWIM_MAX_ASCENSION_SPEED.get(data), data.getYVel() + SWIM_ACCEL.get(data)));
				data.getPlayer().limbAnimator.setSpeed(1.5F); // Kick the legs a little (like when damaged)
			},
			(data, isSelf, seed) -> data.playSound(MarioSFX.SWIM, seed)
	);
}
