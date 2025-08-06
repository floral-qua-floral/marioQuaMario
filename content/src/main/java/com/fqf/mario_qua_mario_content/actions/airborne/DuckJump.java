package com.fqf.mario_qua_mario_content.actions.airborne;

import com.fqf.mario_qua_mario_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.SneakingRule;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.SprintingRule;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.EntireBodyAnimation;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario_api.util.Easing;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.Voicelines;
import com.fqf.mario_qua_mario_content.actions.grounded.DuckWaddle;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DuckJump extends Jump implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("duck_jump");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
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
