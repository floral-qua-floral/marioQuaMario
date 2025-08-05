package com.fqf.mario_qua_mario_content.actions.airborne;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.mariodata.*;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_content.Voicelines;
import com.fqf.mario_qua_mario_content.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario_content.actions.grounded.BonkGroundBackward;
import com.fqf.mario_qua_mario_content.actions.grounded.BonkGroundForward;
import com.fqf.mario_qua_mario_content.util.MarioContentSFX;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class BonkAir extends Fall implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("bonk_air");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	private static LimbAnimation makeArmAnimation(AnimationHelper helper, int factor) {
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			float poseProgress = Math.abs(progress);
			float inversion = Math.signum(progress);
			boolean isTrailingLimb = inversion == factor;

			arrangement.pitch += helper.interpolateKeyframes(poseProgress,
					-64,
					isTrailingLimb ? -20 : -70,
					-50
			);
			arrangement.yaw += helper.interpolateKeyframes(poseProgress,
					factor * 7.5F,
					0,
					factor * 10.5F
			);
			arrangement.roll += helper.interpolateKeyframes(poseProgress,
					factor * 40,
					inversion * (isTrailingLimb ? 72.5F : -60),
					factor * 50
			);

			arrangement.x += helper.interpolateKeyframes(poseProgress,
					0,
					inversion * (isTrailingLimb ? -1 : -1.4F),
					0
			);
			arrangement.y += helper.interpolateKeyframes(poseProgress,
					0,
					isTrailingLimb ? -0.5F : 3.65F,
					0
			);
		});
	}
	private static LimbAnimation makeLegAnimation(AnimationHelper helper, int factor) {
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			float poseProgress = Math.abs(progress);
			float inversion = Math.signum(progress);
			boolean isTrailingLimb = inversion == factor;

			arrangement.pitch += helper.interpolateKeyframes(poseProgress,
					-55,
					isTrailingLimb ? 0 : 30,
					38.865F
			);
			arrangement.yaw += helper.interpolateKeyframes(poseProgress,
					factor * 15,
					inversion * (isTrailingLimb ? 0 : -30),
					factor * -15
			);
			arrangement.roll += helper.interpolateKeyframes(poseProgress,
					0,
					inversion * (isTrailingLimb ? 60 : 30),
					0
			);
			arrangement.x += helper.interpolateKeyframes(poseProgress,
					0,
					inversion * (isTrailingLimb ? -6 : -4),
					0
			);
			arrangement.y += helper.interpolateKeyframes(poseProgress,
					-0.75F,
					isTrailingLimb ? -3 : -2,
					-1F
			);
			arrangement.z += helper.interpolateKeyframes(poseProgress,
					2,
					isTrailingLimb ? 0 : -4,
					4F
			);
		});
	}
	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null,
				new ProgressHandler((data, ticksPassed) -> {
					float deviation = MathHelper.subtractAngles(data.getMario().bodyYaw, data.getVars(BonkVars.class).BONK_YAW);
					return deviation / 180 * 2;
				}),
				new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {
					float poseProgress = Math.abs(progress);
					float inversion = Math.signum(progress);

					arrangement.x += helper.interpolateKeyframes(poseProgress,
							0,
							inversion * -5,
							0
					);
					arrangement.z += helper.interpolateKeyframes(poseProgress,
							0,
							0,
							-2
					);
				}),
				null,
				new BodyPartAnimation((data, arrangement, progress) -> {
					float poseProgress = Math.abs(progress);
					float inversion = Math.signum(progress);

					arrangement.pitch += helper.interpolateKeyframes(poseProgress,
							12.5F,
							0,
							25
					);
					arrangement.roll += helper.interpolateKeyframes(poseProgress,
							0,
							inversion * 37.5F,
							0
					);
				}),
				makeArmAnimation(helper, 1), makeArmAnimation(helper, -1),
				makeLegAnimation(helper, 1), makeLegAnimation(helper, -1),
				null
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

	@Override public @Nullable Identifier getStompTypeID() {
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
				data.playSound(MarioContentSFX.BONK, seed);
				data.voice(Voicelines.BONK, seed);
			}
	);

	private static float yawFromVec3d(Vec3d vector) {
		return 90 + ((float) Math.atan2(vector.z, vector.x)) * MathHelper.DEGREES_PER_RADIAN;
	}

	protected static class BonkVars {
		public final float BONK_YAW;

		public BonkVars(IMarioData data) {
			if(data instanceof IMarioReadableMotionData motionData) {
				this.BONK_YAW = yawFromVec3d(motionData.getVelocity());
			}
			else this.BONK_YAW = data.getMario().bodyYaw;
		}
	}

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new BonkVars(data);
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {
//		data.forceBodyAlignment(true);
//		data.getMario().setBodyYaw(data.getVars(BonkVars.class).BONK_YAW);
	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, AirborneActionHelper helper) {
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
								&& Math.abs(MathHelper.subtractAngles(data.getMario().bodyYaw, data.getVars(BonkVars.class).BONK_YAW)) < 90,
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

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}