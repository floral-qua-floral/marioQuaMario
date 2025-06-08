package com.fqf.mario_qua_mario_content.actions.aquatic;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.ProgressHandler;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.util.ActionTimerVars;
import com.fqf.mario_qua_mario_content.util.MarioContentSFX;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static com.fqf.mario_qua_mario_api.util.StatCategory.*;

public class Swim extends Submerged {
	public static final Identifier ID = MarioQuaMarioContent.makeID("swim");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return Objects.requireNonNull(Submerged.makeAnimation(helper).variate(
				null,
				new ProgressHandler(
						null,
						(data, prevAnimationID) -> true, // Always reset animation progress when entering this action
						(data, ticksPassed) -> Math.min(ticksPassed / 6F, 1)
				),
				null, null, null,
				null, null, null, null,
				null
		));
	}

	public static final CharaStat SWIM_ACCEL = new CharaStat(0.4, SWIMMING, UP, ACCELERATION);
	public static final CharaStat SWIM_MAX_ASCENSION_SPEED = new CharaStat(0.45, SWIMMING, UP, SPEED);

	public static final TransitionDefinition SWIM = new TransitionDefinition(
			ID,
			data -> {
				ActionTimerVars vars = data.getVars(ActionTimerVars.class);
				return (vars == null || vars.actionTimer > 2) && data.getInputs().JUMP.isPressed();
			},
			EvaluatorEnvironment.CLIENT_ONLY,
			data -> {
				data.setYVel(Math.min(SWIM_MAX_ASCENSION_SPEED.get(data), data.getYVel() + SWIM_ACCEL.get(data)));
				data.getMario().limbAnimator.setSpeed(1.5F); // Kick the legs a little (like when damaged)
			},
			(data, isSelf, seed) -> data.playSound(MarioContentSFX.SWIM, seed)
	);
}
