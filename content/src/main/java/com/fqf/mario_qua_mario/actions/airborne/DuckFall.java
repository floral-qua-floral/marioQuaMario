package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.actions.grounded.DuckWaddle;
import com.fqf.mario_qua_mario.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario.definitions.states.actions.util.SneakingRule;
import com.fqf.mario_qua_mario.definitions.states.actions.util.SprintingRule;
import com.fqf.mario_qua_mario.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.LimbAnimation;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.Easing;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.fqf.mario_qua_mario.util.StatCategory.JUMPING_GRAVITY;
import static com.fqf.mario_qua_mario.util.StatCategory.JUMP_VELOCITY;

public class DuckFall extends Fall implements AirborneActionDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("duck_fall");
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return DuckWaddle.makeDuckAnimation(false, true);
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.ALLOW;
	}

	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	public static final TransitionDefinition DUCK_FALL = Fall.FALL.variate(MarioQuaMarioContent.makeID("duck_fall"), null);

	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
		return List.of(
				DuckWaddle.UNDUCK.variate(MarioQuaMarioContent.makeID("fall"), null)
		);
	}

	@Override
	public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper) {
		return List.of(
				Fall.LANDING.variate(MarioQuaMarioContent.makeID("duck_waddle"), null)
		);
	}
}
