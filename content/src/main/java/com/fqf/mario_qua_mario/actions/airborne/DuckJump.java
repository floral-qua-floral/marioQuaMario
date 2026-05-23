package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.SneakingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.SprintingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.EntireBodyAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.PiecemealPlayermodelAnimation;
import com.fqf.charaformact_api.util.Easing;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.grounded.DuckWaddle;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DuckJump extends Jump implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("duck_jump");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}


	@Override
	public @Nullable AnimationDefinition getAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_ARMS,
				(posture, data, animationTime, helper) -> {
					posture.TORSO.setAnglesDegrees(90, 0, 0);
				}
		);
	}

	@Override public @Nullable PiecemealPlayermodelAnimation getOldAnimation(AnimationHelper helper) {
		return DuckWaddle.makeDuckAnimation(false, true).variate(
				null, null,
				new EntireBodyAnimation(0.0F, true, (data, arrangement, progress) -> {
					float tilt_progress = Easing.clampedRangeToProgress(data.getYVel(), -0.0, 0.4);
					arrangement.pitch = (tilt_progress * 2 - 1) * 15F;
				}),
				null, null,
				null, null,
				null, null, null
		);
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.ALLOW;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	public static TransitionDefinition makeDuckJumpTransition(GroundedActionDefinition.GroundedActionHelper helper) {
		return new TransitionDefinition(
				DuckJump.ID,
				data -> data.getInputs().JUMP.isPressed(),
				EvaluatorEnvironment.CLIENT_ONLY,
				data -> helper.performJump(data, JUMP_VEL, JUMP_ADDEND),
				(data, isSelf, seed) -> {
					data.playJumpSound(seed);
					data.voice(Voicelines.DUCK_JUMP, seed);
				}
		);
	}

	@Override protected double getJumpCapThreshold() {
		return 0.14;
	}
	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AirborneActionHelper helper) {
		return List.of(
				DuckWaddle.UNDUCK.variate(Jump.ID, null)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
		return List.of(
				helper.makeJumpCapTransition(this, this.getJumpCapThreshold())
		);
	}

	@Override protected TransitionDefinition getLandingTransition() {
		return DuckFall.DUCK_LANDING;
	}
}
