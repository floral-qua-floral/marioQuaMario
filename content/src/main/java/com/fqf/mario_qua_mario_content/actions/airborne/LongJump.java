package com.fqf.mario_qua_mario_content.actions.airborne;

import com.fqf.mario_qua_mario_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.SneakingRule;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.SprintingRule;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_api.util.Easing;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario_content.actions.grounded.PRun;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.fqf.mario_qua_mario_api.util.StatCategory.JUMP_VELOCITY;
import static com.fqf.mario_qua_mario_api.util.StatCategory.THRESHOLD;

public class LongJump extends Jump implements AirborneActionDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("long_jump");
	}

//	private static LimbAnimation makeArmAnimation(AnimationHelper helper, int factor) {
//		return new LimbAnimation(false, (data, arrangement, progress) -> {
//			arrangement.addAngles(
//					helper.interpolateKeyframes(progress, 0, 0),
//					helper.interpolateKeyframes(progress, 0, 0),
//					helper.interpolateKeyframes(progress, 70, 0)
//			);
//		});
//	}
	private static float interpProgress(AnimationHelper helper, float progress) {
		return helper.sequencedEase(progress, Easing.QUART_OUT, Easing.QUAD_IN);
//		return 2;
	}
	private static float rotationProgress(float progress) {
		return (progress - 1) * 2.4F;
	}
	private static final float BODY_ROTATION_SPEED = 1.2F;
	private static void positionOffset(AnimationHelper helper, float progress, Arrangement arrangement) {
		arrangement.y += helper.interpolateKeyframes(progress * BODY_ROTATION_SPEED, 2.24F, 0, 4.4F);
		arrangement.z += helper.interpolateKeyframes(progress * BODY_ROTATION_SPEED, -1.2F, 0, -2);
	}
	private static LimbAnimation makeLegAnimation(AnimationHelper helper, int factor) {
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			float interpProgress = interpProgress(helper, progress);
			float rotationProgress = rotationProgress(progress);
			arrangement.pitch += helper.interpolateKeyframes(interpProgress, 17, -5 + MathHelper.sin(rotationProgress) * factor * 56.6F, -57);
			arrangement.y += helper.interpolateKeyframes(interpProgress * BODY_ROTATION_SPEED, -1.92F, -0.8F, 0);
			arrangement.z += helper.interpolateKeyframes(interpProgress * BODY_ROTATION_SPEED, 2, -4.2F, 5);
		});
	}
	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
//		PlayermodelAnimation old = new PlayermodelAnimation(
//				null,
//				new ProgressHandler(
//						(data, ticksPassed) ->
//								ticksPassed / 100F
//				),
//				null,
//				new BodyPartAnimation((data, arrangement, progress) -> {
//
//				}),
//				new BodyPartAnimation((data, arrangement, progress) -> {
//					arrangement.pitch -= helper.interpolateKeyframes(progress, 0, 0);
//				}),
//
//				new LimbAnimation(false, (data, arrangement, progress) -> {
//					arrangement.pitch -= helper.interpolateKeyframes(progress, 0, 170, 30);
//					arrangement.yaw += helper.interpolateKeyframes(progress, 0, 0, 70);
//					arrangement.roll += helper.interpolateKeyframes(progress, 70, -17.4F, 88);
//				}),
//				new LimbAnimation(false, (data, arrangement, progress) -> {
//					arrangement.pitch -= helper.interpolateKeyframes(progress, 98, 64);
//					arrangement.yaw -= helper.interpolateKeyframes(progress, 0, 90);
//					arrangement.roll += helper.interpolateKeyframes(progress, 0, 0);
//				}),
//
//				new LimbAnimation(false, (data, arrangement, progress) -> {
//					arrangement.y -= helper.interpolateKeyframes(progress, 2, 0);
//					arrangement.z -= helper.interpolateKeyframes(progress, 2, 0);
//				}),
//				new LimbAnimation(false, (data, arrangement, progress) -> {
//					arrangement.y -= helper.interpolateKeyframes(progress, 0, 2);
//					arrangement.z -= helper.interpolateKeyframes(progress, 0, 2);
//				}),
//				null
//		);
		return new PlayermodelAnimation(
				null,
				new ProgressHandler(
						(data, ticksPassed) ->
								ticksPassed / 8F
				),
				null,
				new BodyPartAnimation((data, arrangement, progress) -> {
					positionOffset(helper, interpProgress(helper, progress), arrangement);
				}),
				new BodyPartAnimation((data, arrangement, progress) -> {
					float interpProgress = interpProgress(helper, progress);
					arrangement.pitch += helper.interpolateKeyframes(interpProgress * BODY_ROTATION_SPEED, 33, -26, 36);
					positionOffset(helper, interpProgress, arrangement);
				}),

				new LimbAnimation(false, (data, arrangement, progress) -> {
					float interpProgress = interpProgress(helper, progress);
					float rotationProgress = rotationProgress(progress);
					arrangement.addAngles(
							helper.interpolateKeyframes(interpProgress, 0, -75 + MathHelper.sin(rotationProgress) * 28.5F, 65),
							helper.interpolateKeyframes(interpProgress, 0, 63 + MathHelper.cos(rotationProgress) * 41, -5),
							helper.interpolateKeyframes(interpProgress, 70, 0, 30)
					);
					positionOffset(helper, interpProgress, arrangement);
				}),
				new LimbAnimation(false, (data, arrangement, progress) -> {
					float interpProgress = interpProgress(helper, progress);
					float rotationProgress = rotationProgress(progress) + MathHelper.PI * 0.7F;
					arrangement.addAngles(
							helper.interpolateKeyframes(interpProgress, -90, -75 + MathHelper.cos(rotationProgress) * 28.5F, 65),
							helper.interpolateKeyframes(interpProgress, 0, -63 + MathHelper.sin(rotationProgress) * 41, 5),
							helper.interpolateKeyframes(interpProgress, 0, 0, -30)
					);
					positionOffset(helper, interpProgress, arrangement);
				}),

				makeLegAnimation(helper, 1),
				makeLegAnimation(helper, -1),
				null
		);
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.ALLOW;
	}

	public static final CharaStat FALL_ACCEL = Fall.FALL_ACCEL.variate(0.575);

	public static final CharaStat LONG_JUMP_VEL = new CharaStat(0.614, JUMP_VELOCITY);
	public static final CharaStat LONG_JUMP_THRESHOLD = new CharaStat(0.285, THRESHOLD);

	public static CharaStat REDUCED_FORWARD_ACCEL = Fall.FORWARD_DRIFT_ACCEL.variate(0.5);
	public static CharaStat REDUCED_BACKWARD_ACCEL = Fall.BACKWARD_DRIFT_ACCEL.variate(0.5);
	public static CharaStat REDUCED_STRAFE_ACCEL = Fall.STRAFE_DRIFT_ACCEL.variate(0.5);

	public static CharaStat REDUCED_REDIRECTION = Fall.DRIFT_REDIRECTION.variate(0.66);

	@Override
	public void travelHook(IMarioTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, FALL_ACCEL, null, Fall.FALL_SPEED);
		helper.airborneAccel(data,
				REDUCED_FORWARD_ACCEL, Fall.FORWARD_DRIFT_SPEED,
				REDUCED_BACKWARD_ACCEL, Fall.BACKWARD_DRIFT_SPEED,
				REDUCED_STRAFE_ACCEL, Fall.STRAFE_DRIFT_SPEED,
				data.getForwardVel(), data.getStrafeVel(), REDUCED_REDIRECTION
		);
	}

	@Override protected double getJumpCapThreshold() {
		return 0.4;
	}

	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
		return List.of();
	}

	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper) {
		return List.of(
				Submerged.SUBMERGE,
				Jump.DOUBLE_JUMPABLE_LANDING.variate(MarioQuaMarioContent.makeID("p_run"), data ->
						Fall.LANDING.evaluator().shouldTransition(data) && (data.isServer() || PRun.meetsPRunRequirements(data))),
				Jump.DOUBLE_JUMPABLE_LANDING
		);
	}
}
