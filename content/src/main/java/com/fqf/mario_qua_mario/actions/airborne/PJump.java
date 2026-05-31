package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.EvaluatorEnvironment;
import com.fqf.charaformact_api.definitions.states.actions.util.SneakingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.SprintingRule;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.LimbAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.PiecemealPlayermodelAnimation;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario.actions.grounded.PRun;
import com.fqf.mario_qua_mario.actions.form.TailFly;
import com.fqf.mario_qua_mario.actions.form.TailStall;
import com.fqf.mario_qua_mario.actions.wallbound.WallSlide;
import com.fqf.mario_qua_mario.forms.Raccoon;
import com.fqf.mario_qua_mario.util.ClimbTransitions;
import com.fqf.mario_qua_mario.util.Powers;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PJump extends Jump implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("p_jump");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	@Override public @NotNull AnimationDefinition getAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(posture, data, animationTime, helper) -> {
					helper.symmetricallyAnimate(posture, posture.RIGHT_ARM, arrangement ->
							arrangement.addAngles(15, 0, 90));

					helper.asymmetricallyAnimate(posture.RIGHT_LEG, posture.LEFT_LEG, (arrangement, isLeft, sideFactor) ->
							arrangement.pitch += 40);
				}
		);
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.ALLOW;
	}

	@Override
	protected double getJumpCapThreshold() {
		return 0.4;
	}

	@Override
	public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						TailFly.ID,
						data ->
								data.hasPower(Powers.TAIL_FLY)
								&& data.retrieveStateData(Raccoon.RaccoonVars.class).flightTicks > 0
								&& (data.isServer() || (
										data.getYVel() < TailStall.STALL_THRESHOLD.get(data)
										&& data.getInputs().JUMP.isHeld()
								)),
						EvaluatorEnvironment.CLIENT_CHECKED
				),
				GroundPoundFlip.GROUND_POUND,
				helper.makeJumpCapTransition(this, this.getJumpCapThreshold())
		);
	}

	@Override
	public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper) {
		return List.of(
				Submerged.SUBMERGE,
				Jump.DOUBLE_JUMPABLE_LANDING.variate(PRun.ID, data ->
						Fall.LANDING.evaluator().shouldTransition(data) && (data.isServer() || PRun.meetsPRunRequirements(data)),
						EvaluatorEnvironment.CLIENT_CHECKED,
						null,
						null),
				Jump.DOUBLE_JUMPABLE_LANDING,
				ClimbTransitions.CLIMB_NON_SOLID_DIRECTIONAL,
				ClimbTransitions.CLIMB_NON_SOLID_NON_DIRECTIONAL,
				ClimbTransitions.CLIMB_SOLID,
				WallSlide.WALL_SLIDE
		);
	}
}
