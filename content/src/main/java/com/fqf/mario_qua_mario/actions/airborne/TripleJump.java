package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.ActionCategory;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraProgressHandler;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.charaformact_api.util.Easing;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.util.MarioVars;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class TripleJump extends Jump implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("triple_jump");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override
	public @Nullable AnimationDefinition getAnimation() {
		return AnimationDefinition.of(
				null,
				AnimationFlag.NO_SWING_LIMBS, AnimationFlag.Execution.RANDOMLY_MIRROR,
				(arrangement, data, animationTime, helper) -> {
					float progress = helper.sequencedEase(helper.sequencedEase(animationTime / 5F,
									Easing.LINEAR, Easing.LINEAR, Easing.LINEAR, Easing.LINEAR, Easing.LINEAR) / 3, Easing.LINEAR,
							Easing.LINEAR) * 3; // ????
					arrangement.pitch -= Math.min(progress, 4) * 180;
				},
				(posture, data, animationTime, helper) -> {
					float progress = helper.sequencedEase(helper.sequencedEase(animationTime / 5F,
							Easing.LINEAR, Easing.LINEAR, Easing.LINEAR, Easing.LINEAR, Easing.LINEAR) / 3, Easing.LINEAR,
							Easing.LINEAR) * 3; // ????

//					posture.EVERYTHING.pitch -= Math.min(progress, 4) * 180;

					helper.symmetricallyAnimate(posture, posture.RIGHT_ARM, arrangement -> arrangement.addAngles(
							helper.interpolateKeyframes(progress,
									0,
									0,
									-90,
									-90,
									0,
									0
							),
							0,
							helper.interpolateKeyframes(progress,
									170,
									90,
									0,
									0,
									90,
									107
							)
					));

					helper.symmetricallyAnimate(posture, posture.RIGHT_LEG, (arrangement, isLeft, leftFactor) -> {
						arrangement.pitch += helper.interpolateKeyframes(progress,
								0,
								52,
								0,
								-90 + leftFactor * 20,
								leftFactor * -38.5F,
								isLeft ? -9.5F : 9.1F
						);
						arrangement.addPos(
								0,
								helper.interpolateKeyframes(progress,
										0,
										0,
										0,
										0,
										leftFactor * -3.5F,
										leftFactor * -4.5F
								),
								helper.interpolateKeyframes(progress,
										0,
										0,
										0,
										0,
										leftFactor * -3.25F,
										leftFactor * -4.25F
								)
						);
					});
				}
		);
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return new CameraAnimationSet(
				MarioQuaMario.CONFIG::getTripleJumpCameraAnim,
				new CameraAnimation(
					new CameraProgressHandler(2,
							(data, ticksPassed) -> helper.sequencedEase(ticksPassed / 9.8F, Easing.mix(Easing.SINE_IN_OUT, Easing.LINEAR), Easing.SINE_OUT)
					),
					(data, arrangement, progress) -> arrangement.pitch += progress * 360
				),
				new CameraAnimation(
						new CameraProgressHandler((data, ticksPassed) -> Easing.mixedEase(Easing.QUAD_IN_OUT, Easing.SINE_IN_OUT, Math.min(ticksPassed / 14, 1))),
						(data, arrangement, progress) -> arrangement.pitch += progress * 360
				),
				null
		);
	}

	public static final CfaStat TRIPLE_JUMP_VEL = new CfaStat(1.175, JUMP_VELOCITY);
	public static CfaStat TRIPLE_JUMP_SPEED_THRESHOLD = new CfaStat(0.34, RUNNING, FORWARD, THRESHOLD);

	@Override protected double getJumpCapThreshold() {
		return 0.65;
	}
	@Override protected TransitionDefinition getLandingTransition() {
		return Fall.LANDING;
	}

	private TransitionInjectionDefinition makeInjection(Identifier injectNearTransitionsTo) {
		return new TransitionInjectionDefinition(
				TransitionInjectionDefinition.InjectionPlacement.BEFORE,
				injectNearTransitionsTo,
				ActionCategory.GROUNDED,
				(nearbyTransition, castableHelper) -> nearbyTransition.variate(
						this.getID(),
						data ->
								MarioVars.get(data).canTripleJumpTicks > 0
								&& data.getForwardVel() >= TRIPLE_JUMP_SPEED_THRESHOLD.get(data)
								&& nearbyTransition.evaluator().shouldTransition(data),
						null,
						data -> ((GroundedActionDefinition.GroundedActionHelper) castableHelper)
									.performJump(data, TRIPLE_JUMP_VEL, null),
						(data, isSelf, seed) -> {
							data.playJumpSound(seed);
							data.voice(Voicelines.TRIPLE_JUMP, seed);
						}
				)
		);
	}
	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of(
			this.makeInjection(Jump.ID),
			this.makeInjection(PJump.ID)
		);
	}
}
