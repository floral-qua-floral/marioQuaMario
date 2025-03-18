package com.fqf.mario_qua_mario.actions.aquatic;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.MarioContentSFX;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.fqf.mario_qua_mario.util.StatCategory.*;

public class Swim extends Submerged {
	public static final Identifier ID = MarioQuaMarioContent.makeID("swim");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	private static LimbAnimation makeArmAnimation(int factor) {
	    return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.roll = MathHelper.lerp(progress, factor * 90, 0);
	    });
	}
	private static LimbAnimation makeLegAnimation(int factor) {
	    return new LimbAnimation(false, (data, arrangement, progress) -> {

	    });
	}
	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
	    return new PlayermodelAnimation(
	            null,
	            new ProgressHandler(
						null,
						(data, prevAnimationID) -> true,
						(data, ticksPassed) -> ticksPassed / 3F
				),
	            new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {

	            }),
	            null,
	            null,
	            makeArmAnimation(1), makeArmAnimation(-1),
	            makeLegAnimation(1), makeLegAnimation(-1),
	            null
	    );
	}

	public static final CharaStat SWIM_ACCEL = new CharaStat(0.4, SWIMMING, UP, ACCELERATION);
	public static final CharaStat SWIM_MAX_ASCENSION_SPEED = new CharaStat(0.45, SWIMMING, UP, SPEED);

	public static final TransitionDefinition SWIM = new TransitionDefinition(
			ID,
			data -> data.getVars(ActionTimerVars.class).actionTimer > 2 && data.getInputs().JUMP.isPressed(),
			EvaluatorEnvironment.CLIENT_ONLY,
			data -> {
				data.setYVel(Math.min(SWIM_MAX_ASCENSION_SPEED.get(data), data.getYVel() + SWIM_ACCEL.get(data)));
			},
			(data, isSelf, seed) -> data.playSound(MarioContentSFX.SWIM, seed)
	);
}
