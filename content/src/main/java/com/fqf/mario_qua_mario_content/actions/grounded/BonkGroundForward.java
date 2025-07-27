package com.fqf.mario_qua_mario_content.actions.grounded;

import com.fqf.mario_qua_mario_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.util.Easing;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.util.ActionTimerVars;
import com.fqf.mario_qua_mario_content.util.StandUpWithKneeAnimation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BonkGroundForward extends BonkGroundBackward implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("bonk_ground_forward");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	private static LimbAnimation makeArmAnimation(AnimationHelper helper, int factor) {
		boolean isRight = factor == 1;
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.pitch += helper.interpolateKeyframes(progress,
				-80,
				isRight ? 0 : -110,
				0
			);
			arrangement.yaw += helper.interpolateKeyframes(progress,
				factor * -80,
				isRight ? 0 : 37.5F,
				isRight ? 0 : Easing.QUART_IN.ease(progress - 1, 90F, 0)
			);
			arrangement.roll += helper.interpolateKeyframes(progress,
				factor * 90,
				isRight ? 0 : -90,
				0
			);
			arrangement.y += helper.interpolateKeyframes(progress,
					0,
					isRight ? 1 : 0,
					0
			);
			arrangement.z += helper.interpolateKeyframes(progress,
				1.25F,
				isRight ? 0 : 1,
				0
			);
		});
	}
	private static LimbAnimation makeLegAnimation(AnimationHelper helper, int factor) {
		boolean isRight = factor == 1;
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.pitch += helper.interpolateKeyframes(progress,
					87.5F,
					isRight ? 90 : 15,
					0
			);
			arrangement.y += helper.interpolateKeyframes(progress,
					-1.25F,
					isRight ? 0 : -8.5F,
					0
			);
			arrangement.z += helper.interpolateKeyframes(progress,
					1.5F,
					isRight ? -2 : -3,
					0
			);
		});
	}
	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return StandUpWithKneeAnimation.getAnimation(
				helper, (data, ticksPassed) -> data.getVars(ActionTimerVars.class).actionTimer / (float) STANDUP_TICKS,
				-3.25F, 40,
				-80, -80, 90, 1.25F,
				87.5F, 0, 1.5F,
				90, 0, 0, -2
		);
//		return new PlayermodelAnimation(
//				null,
//				new ProgressHandler((data, ticksPassed) -> 2 * Easing.SINE_IN_OUT.ease(Math.min(1, data.getVars(ActionTimerVars.class).actionTimer / (float) STANDUP_TICKS))),
//				new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {
//					arrangement.y += helper.interpolateKeyframes(progress,
//							-9.75F,
//							-8.5F,
//							0
//					);
//					arrangement.z += helper.interpolateKeyframes(progress,
//							-3.25F,
//							0,
//							0
//					);
//				}),
//				new BodyPartAnimation((data, arrangement, progress) -> {
//
//				}),
//				new BodyPartAnimation((data, arrangement, progress) -> {
//					arrangement.pitch += helper.interpolateKeyframes(progress,
//							40,
//							25,
//							0
//					);
//				}),
//				makeArmAnimation(helper, 1), makeArmAnimation(helper, -1),
//				makeLegAnimation(helper, 1), makeLegAnimation(helper, -1),
//				null
//		);
	}
}
