package com.fqf.mario_qua_mario.util;

import com.fqf.charaformact_api.cfadata.CfaAnimatingData;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.*;
import com.fqf.charaformact_api.util.Easing;

public class StandUpWithKneeAnimation {
	@FunctionalInterface
	public interface ProgressCalculator {
		float calculateProgress(CfaAnimatingData data, float animationTime);
	}

	public static ProgressCalculator makeProgressCalculator(float animationDuration) {
		return (data, animationTime) -> 2 * Easing.SINE_IN_OUT.ease(Math.min(1, animationTime / animationDuration));
	}
	public static AnimationDefinition makeAnimation(
			ProgressCalculator progressCalculator,
			float everythingZ1, float bodyPitch1,
			float armPitch1, float armYaw1, float armRoll1, float armZ1,
			float legPitch1, float legYaw1, float legZ1,
			float legRightPitch2, float legRightYaw2, float legRightY2, float legRightZ2
	) {
		return makeAnimation(
				progressCalculator,
				everythingZ1, bodyPitch1,
				armPitch1, armYaw1, armRoll1, armZ1,
				legPitch1, legYaw1, legZ1,
				legRightPitch2, legRightYaw2, legRightY2, legRightZ2,
				0, 0,
				0, 0, 0,
				0, 0, 0, 0, 0
		);
	}
	public static AnimationDefinition makeAnimation(
			ProgressCalculator progressCalculator,
			float everythingZ1, float bodyPitch1,
			float armPitch1, float armYaw1, float armRoll1, float armZ1,
			float legPitch1, float legYaw1, float legZ1,
			float legRightPitch2, float legRightYaw2, float legRightY2, float legRightZ2,
			float bodyPitch3, float bodyY3,
			float armPitch3, float armYaw3, float armRoll3,
			float legPitch3, float legRoll3, float legX3, float legY3, float legZ3
	) {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(data, prevAnimation) -> switch(data.getCurrentHandPreference()) {
					case PREFER_RIGHT -> AnimationFlag.Execution.ONLY_MIRROR;
					case PREFER_LEFT -> AnimationFlag.Execution.NONE;
					case EITHER, NEITHER -> AnimationFlag.Execution.RANDOMLY_MIRROR.apply(data, prevAnimation);
				},
				(arrangement, data, animationTime, helper) -> {
					float progress = progressCalculator.calculateProgress(data, animationTime);
					arrangement.addPos(
							0,
							helper.interpolateKeyframes(progress,
									-9.75F,
									-8.5F,
									bodyY3
							),
							helper.interpolateKeyframes(progress,
									everythingZ1,
									0,
									0
							)
					);
				},
				(posture, data, animationTime, helper) -> {
					float progress = progressCalculator.calculateProgress(data, animationTime);

					posture.TORSO.pitch += helper.interpolateKeyframes(progress,
							bodyPitch1,
							25,
							bodyPitch3
					);

					helper.asymmetricallyAnimate(posture.RIGHT_ARM, posture.LEFT_ARM, (arrangement, isLeft, sideFactor) -> {
						arrangement.addPos(
								0,
								helper.interpolateKeyframes(progress,
										0,
										isLeft ? 0 : 1,
										0
								),
								helper.interpolateKeyframes(progress,
										armZ1,
										isLeft ? 1 : 0,
										0
								)
						);
						arrangement.addAngles(
								helper.interpolateKeyframes(progress,
										armPitch1,
										isLeft ? -110 : 0,
										armPitch3
								),
								helper.interpolateKeyframes(progress,
										sideFactor * armYaw1,
										isLeft ? 37.5F : 0,
										isLeft ? Easing.QUART_IN.ease(progress - 1, 90F, sideFactor * armYaw3) : sideFactor * armYaw3
								),
								helper.interpolateKeyframes(progress,
										sideFactor * armRoll1,
										isLeft ? -90 : 0,
										sideFactor * armRoll3
								)
						);
					});

					helper.asymmetricallyAnimate(posture.RIGHT_LEG, posture.LEFT_LEG, (arrangement, isLeft, sideFactor) -> {
						arrangement.addPos(
								helper.interpolateKeyframes(progress,
										0,
										0,
										sideFactor * legX3
								),
								helper.interpolateKeyframes(progress,
										-1.25F,
										isLeft ? -8.5F : legRightY2,
										legY3
								),
								helper.interpolateKeyframes(progress,
										legZ1,
										isLeft ? -3 : legRightZ2,
										legZ3
								)
						);
						arrangement.addAngles(
								helper.interpolateKeyframes(progress,
										legPitch1,
										isLeft ? 15 : legRightPitch2,
										legPitch3
								),
								helper.interpolateKeyframes(progress,
										sideFactor * legYaw1,
										isLeft ? 0 : sideFactor * legRightYaw2,
										0
								),
								helper.interpolateKeyframes(progress,
										0,
										0,
										sideFactor * legRoll3
								)
						);
					});

					if(posture.TAIL != null) {
						posture.TAIL.pitch = posture.TAIL.pitch * progress / 2 - posture.TORSO.pitch;
					}
				}
		);
	}
}
