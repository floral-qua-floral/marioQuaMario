package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.definitions.TransitionInjectionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.GenericActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionTransitionDetails;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.Easing;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.util.MarioVars;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class DoubleJump extends Jump implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("double_jump");
	@Override public @NotNull Identifier defineID() {
	    return ID;
	}

	public static final AnimationDefinition ANIMATION = AnimationDefinition.of(
			AnimationFlag.NO_SWING_LIMBS,
			AnimationFlag.Execution.RANDOMLY_MIRROR,
			(posture, data, animationTime, helper) -> {
				float progress = Jump.getAnimationProgress(data);

				posture.HEAD.pitch += MathHelper.lerp(progress, -13, 27.5F);
				posture.TORSO.yaw += progress * -10;

				helper.symmetricallyAnimate(posture, posture.RIGHT_ARM, arrangement -> {
					arrangement.addAngles(Easing.BACK_IN.ease((1 - progress)) * 26, 0, progress * 70);
					arrangement.addPos(progress * -0.345F, Easing.BACK_OUT.ease(progress, 1.1F, -2.333F), 0);
				});

				posture.RIGHT_LEG.pitch += MathHelper.lerp(progress, 20, 9.1F);
				posture.RIGHT_LEG.y -= progress * 4.5F;
				posture.RIGHT_LEG.z -= progress * 4.25F;

				posture.LEFT_LEG.pitch += MathHelper.lerp(progress, 20, -9.5F);
			}
	);
	@Override public @Nullable AnimationDefinition defineAnimation() {
		return ANIMATION;
	}
	@Override public @Nullable CameraAnimationSet defineCameraAnimations(AnimationHelper helper) {
		return null;
	}

	public static final CfaStat DOUBLE_JUMP_VEL = new CfaStat(0.939, JUMP_VELOCITY);
	public static final CfaStat DOUBLE_JUMP_ADDEND = new CfaStat(0.3, JUMP_VELOCITY);
	public static CfaStat DOUBLE_JUMP_SPEED_THRESHOLD = new CfaStat(0, WALKING, FORWARD, THRESHOLD);

	public static final ActionTransitionDetails TRIPLE_JUMPABLE_LANDING = Fall.LANDING.variate(
			null, null, null,
			data -> MarioVars.get(data).canTripleJumpTicks = 3,
			null
	);

	@Override protected double getJumpCapThreshold() {
		return 0.285;
	}
	@Override protected ActionTransitionDetails getLandingTransition() {
		return TRIPLE_JUMPABLE_LANDING;
	}

	public static TransitionInjectionDefinition makeTransitionInjection(
			Identifier transitionTo,
			ToIntFunction<MarioVars> tickChecker, CfaStat requiredForwardSpeed,
			CfaStat jumpVel, CfaStat addend, Identifier voiceline
	) {
		return new TransitionInjectionDefinition() {
			@Override
			public @Nullable InjectionPlacement getPlacementRelativeTo(ActionCategory fromCategory, Identifier fromID, ActionCategory toCategory, Identifier toID) {
				return (fromCategory == ActionCategory.GROUNDED && (toID == Jump.ID || toID == PJump.ID)) ? InjectionPlacement.BEFORE : null;
			}

			@Override
			public @NotNull ActionTransitionDetails makeTransition(ActionTransitionDetails nearbyTransition, GenericActionDefinition.CastableHelper helper) {
				return nearbyTransition.variate(
						transitionTo,
						data ->
								tickChecker.applyAsInt(MarioVars.get(data)) > 0
								&& data.getForwardVel() >= requiredForwardSpeed.getAsLimit(data)
								&& nearbyTransition.evaluator().shouldTransition(data),
						null,
						data -> helper.asGrounded().performJump(data, jumpVel, addend),
						(data, isSelf, seed) -> {
							data.playJumpSound(seed);
							data.voice(voiceline, seed);
						}
				);
			}
		};
	}

	public static final TransitionInjectionDefinition INJECTION = makeTransitionInjection(
			DoubleJump.ID,
			vars -> vars.canDoubleJumpTicks,
			DOUBLE_JUMP_SPEED_THRESHOLD,
			DOUBLE_JUMP_VEL, DOUBLE_JUMP_ADDEND, Voicelines.DOUBLE_JUMP
	);
}
