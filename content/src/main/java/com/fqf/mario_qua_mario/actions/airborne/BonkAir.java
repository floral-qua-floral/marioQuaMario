package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.cfadata.*;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario.actions.grounded.BonkGroundBackward;
import com.fqf.mario_qua_mario.actions.grounded.BonkGroundForward;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class BonkAir extends Fall implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("bonk_air");
	@Override public @NotNull Identifier defineID() {
		return ID;
	}

	public static float getDeviation(CfaAnimatingData data) {
		return MathHelper.subtractAngles(data.getPlayer().bodyYaw, data.retrieveStateData(BonkAir.BonkVars.class).recalculateBonkYaw(data)) / 180 * 2;
	}

	@Override public @Nullable AnimationDefinition getAnimation() {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(arrangement, data, animationTime, helper) -> {
					float deviation = getDeviation(data);
					float poseProgress = Math.abs(deviation);
					float inversion = Math.signum(deviation);

					arrangement.addPos(
							helper.interpolateKeyframes(poseProgress,
									0,
									inversion * -5,
									0
							),
							0,
							helper.interpolateKeyframes(poseProgress,
									0,
									0,
									-2
							)
					);
				},
				(posture, data, animationTime, helper) -> {
					float deviation = getDeviation(data);
					float poseProgress = Math.abs(deviation);
					float inversion = Math.signum(deviation);

					posture.TORSO.addAngles(
							helper.interpolateKeyframes(poseProgress,
									12.5F,
									0,
									25
							),
							0,
							helper.interpolateKeyframes(poseProgress,
									0,
									inversion * 37.5F,
									0
							)
					);

					helper.asymmetricallyAnimate(posture.RIGHT_ARM, posture.LEFT_ARM, (arrangement, isLeft, sideFactor) -> {
						boolean isTrailingLimb = inversion == sideFactor;
						arrangement.addPos(
								helper.interpolateKeyframes(poseProgress,
										0,
										inversion * (isTrailingLimb ? -1 : -1.4F),
										0
								),
								helper.interpolateKeyframes(poseProgress,
										0,
										isTrailingLimb ? -0.5F : 3.65F,
										0
								),
								0
						);
						arrangement.addAngles(
								helper.interpolateKeyframes(poseProgress,
										-64,
										isTrailingLimb ? -20 : -70,
										-50
								),
								helper.interpolateKeyframes(poseProgress,
										sideFactor * 7.5F,
										0,
										sideFactor * 10.5F
								),
								helper.interpolateKeyframes(poseProgress,
										sideFactor * 40,
										inversion * (isTrailingLimb ? 72.5F : -60),
										sideFactor * 50
								)
						);
					});

					helper.asymmetricallyAnimate(posture.RIGHT_LEG, posture.LEFT_LEG, (arrangement, isLeft, sideFactor) -> {
						boolean isTrailingLimb = inversion == sideFactor;

						arrangement.addPos(
								helper.interpolateKeyframes(poseProgress,
										0,
										inversion * (isTrailingLimb ? -6 : -4),
										0
								),
								helper.interpolateKeyframes(poseProgress,
										-0.75F,
										isTrailingLimb ? -3 : -2,
										-1F
								),
								helper.interpolateKeyframes(poseProgress,
										2,
										isTrailingLimb ? 0 : -4,
										4F
								)
						);

						arrangement.addAngles(
								helper.interpolateKeyframes(poseProgress,
										-55,
										isTrailingLimb ? 0 : 30,
										38.865F
								),
								helper.interpolateKeyframes(poseProgress,
										sideFactor * 15,
										inversion * (isTrailingLimb ? 0 : -30),
										sideFactor * -15
								),
								helper.interpolateKeyframes(poseProgress,
										0,
										inversion * (isTrailingLimb ? 60 : 30),
										0
								)
						);
					});
				}
		);
	}

	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable Identifier getCollisionAttackTypeID() {
		return null;
	}

	public static final TransitionDefinition BONK = new TransitionDefinition(
			BonkAir.ID,
			data -> data.getRecordedCollisions().collidedHorizontally(),
			EvaluatorEnvironment.CLIENT_ONLY,
			data -> {
				data.setVelocity(data.getRecordedCollisions().getHorizontallyReflectedVelocity().multiply(0.7));
			},
			(data, isSelf, seed) -> {
				data.playSound(MarioSFX.BONK, seed);
				data.voice(Voicelines.BONK, seed);
			}
	);

	private static float yawFromVec3d(Vec3d vector) {
		return 90 + ((float) Math.atan2(vector.z, vector.x)) * MathHelper.DEGREES_PER_RADIAN;
	}

	protected static class BonkVars {
		public float bonkYaw;
		public int noInputTicks;

		public BonkVars(CfaData data) {
			this.noInputTicks = 2;
			BonkVars oldVars = data.retrieveStateData(BonkVars.class);
			if(oldVars != null)
				this.bonkYaw = oldVars.bonkYaw;
			else if(data instanceof CfaReadableMotionData motionData)
				this.recalculateBonkYaw(motionData);
			else
				this.bonkYaw = data.getPlayer().bodyYaw;
		}

		public float recalculateBonkYaw(CfaReadableMotionData data) {
			if(data.getHorizVelSquared() != 0) {
				this.bonkYaw = yawFromVec3d(data.getVelocity());
			}
			return this.bonkYaw;
		}
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new BonkVars(data);
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {
//		data.forceBodyAlignment(true);
//		data.getPlayer().setBodyYaw(data.retrieveStateData(BonkVars.class).BONK_YAW);
	}
	@Override public void serverTick(CfaAuthoritativeData data) {

	}
	@Override public void travelHook(CfaTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, Fall.FALL_ACCEL, null, Fall.FALL_SPEED);
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AirborneActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper) {
		return List.of(
				Submerged.SUBMERGE,
				Fall.LANDING.variate(
						BonkGroundBackward.ID,
						data -> Fall.LANDING.evaluator().shouldTransition(data)
								&& Math.abs(MathHelper.subtractAngles(data.getPlayer().bodyYaw, data.retrieveStateData(BonkVars.class).bonkYaw)) < 90,
						EvaluatorEnvironment.CLIENT_ONLY,
						null,
						null
				),
				Fall.LANDING.variate(BonkGroundForward.ID, null)
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

}