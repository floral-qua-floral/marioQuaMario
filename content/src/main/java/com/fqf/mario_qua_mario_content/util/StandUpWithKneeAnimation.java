package com.fqf.mario_qua_mario_content.util;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.util.Easing;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;

public class StandUpWithKneeAnimation {

	private static LimbAnimation makeArmAnimation(
			AnimationHelper helper, int factor,
			float armPitch1, float armYaw1, float armRoll1, float armZ1,
			float armPitch3, float armYaw3, float armRoll3
	) {
		boolean isRight = factor == 1;
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.pitch += helper.interpolateKeyframes(progress,
					armPitch1,
					isRight ? 0 : -110,
					armPitch3
			);
			arrangement.yaw += helper.interpolateKeyframes(progress,
					factor * armYaw1,
					isRight ? 0 : 37.5F,
					isRight ? factor * armYaw3 : Easing.QUART_IN.ease(progress - 1, 90F, factor * armYaw3)
			);
			arrangement.roll += helper.interpolateKeyframes(progress,
					factor * armRoll1,
					isRight ? 0 : -90,
					factor * armRoll3
			);
			arrangement.y += helper.interpolateKeyframes(progress,
					0,
					isRight ? 1 : 0,
					0
			);
			arrangement.z += helper.interpolateKeyframes(progress,
					armZ1,
					isRight ? 0 : 1,
					0
			);
		});
	}
	private static LimbAnimation makeLegAnimation(
			AnimationHelper helper, int factor,
			float legPitch1, float legYaw1, float legZ1,
			float legRightPitch2, float legRightYaw2,
			float legRightY2, float legRightZ2,
			float legPitch3, float legRoll3,
			float legX3, float legY3, float legZ3
	) {
		boolean isRight = factor == 1;
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.pitch += helper.interpolateKeyframes(progress,
					legPitch1,
					isRight ? legRightPitch2 : 15,
					legPitch3
			);
			arrangement.yaw += helper.interpolateKeyframes(progress,
					factor * legYaw1,
					isRight ? factor * legRightYaw2 : 0,
					0
			);
			arrangement.roll += helper.interpolateKeyframes(progress,
					0,
					0,
					factor * legRoll3
			);
			arrangement.x += helper.interpolateKeyframes(progress,
					0,
					0,
					factor * legX3
			);
			arrangement.y += helper.interpolateKeyframes(progress,
					-1.25F,
					isRight ? legRightY2 : -8.5F,
					legY3
			);
			arrangement.z += helper.interpolateKeyframes(progress,
					legZ1,
					isRight ? legRightZ2 : -3,
					legZ3
			);
		});
	}

	public static PlayermodelAnimation makeAnimation(
			AnimationHelper helper,
			ProgressHandler.ProgressCalculator progressCalculator,
			float everythingZ1, float bodyPitch1,
			float armPitch1, float armYaw1, float armRoll1, float armZ1,
			float legPitch1, float legYaw1, float legZ1,
			float legRightPitch2, float legRightYaw2, float legRightY2, float legRightZ2
	) {
		return makeAnimation(
				helper, progressCalculator,
				everythingZ1, bodyPitch1,
				armPitch1, armYaw1, armRoll1, armZ1,
				legPitch1, legYaw1, legZ1,
				legRightPitch2, legRightYaw2, legRightY2, legRightZ2,
				0, 0,
				0, 0, 0,
				0, 0, 0, 0, 0
		);
	}

	public static PlayermodelAnimation makeAnimation(
			AnimationHelper helper,
			ProgressHandler.ProgressCalculator progressCalculator,
			float everythingZ1, float bodyPitch1,
			float armPitch1, float armYaw1, float armRoll1, float armZ1,
			float legPitch1, float legYaw1, float legZ1,
			float legRightPitch2, float legRightYaw2, float legRightY2, float legRightZ2,
			float bodyPitch3, float bodyY3,
			float armPitch3, float armYaw3, float armRoll3,
			float legPitch3, float legRoll3, float legX3, float legY3, float legZ3
	) {
		return new PlayermodelAnimation(
				(data, rightArmBusy, leftArmBusy, headRelativeYaw) -> {
					if(rightArmBusy && !leftArmBusy) return false;
					if(leftArmBusy && !rightArmBusy) return true;
					return data.getMario().getRandom().nextBoolean();
				},
				new ProgressHandler((data, ticksPassed) -> 2 * Easing.SINE_IN_OUT.ease(Math.min(1, progressCalculator.calculateProgress(data, ticksPassed)))),
				new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {
					arrangement.y += helper.interpolateKeyframes(progress,
							-9.75F,
							-8.5F,
							bodyY3
					);
					arrangement.z += helper.interpolateKeyframes(progress,
							everythingZ1,
							0,
							0
					);
				}),
				new BodyPartAnimation((data, arrangement, progress) -> {

				}),
				new BodyPartAnimation((data, arrangement, progress) -> {
					arrangement.pitch += helper.interpolateKeyframes(progress,
							bodyPitch1,
							25,
							bodyPitch3
					);
				}),
				makeArmAnimation(helper, 1, armPitch1, armYaw1, armRoll1, armZ1, armPitch3, armYaw3, armRoll3),
				makeArmAnimation(helper, -1, armPitch1, armYaw1, armRoll1, armZ1, armPitch3, armYaw3, armRoll3),
				makeLegAnimation(helper, 1, legPitch1, legYaw1, legZ1, legRightPitch2, legRightYaw2, legRightY2, legRightZ2, legPitch3, legRoll3, legX3, legY3, legZ3),
				makeLegAnimation(helper, -1, legPitch1, legYaw1, legZ1, legRightPitch2, legRightYaw2, legRightY2, legRightZ2, legPitch3, legRoll3, legX3, legY3, legZ3),
				new LimbAnimation(true, (data, arrangement, progress) -> {
					arrangement.pitch *= progress / 2;
				})
		);
	}

}
