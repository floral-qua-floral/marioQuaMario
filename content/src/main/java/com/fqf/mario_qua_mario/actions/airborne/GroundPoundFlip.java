package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationFlag;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraProgressHandler;
import com.fqf.charaformact_api.definitions.states.attachments.StateAttachmentKey;
import com.fqf.charaformact_api.util.Easing;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.aquatic.AquaticPoundFlip;
import com.fqf.mario_qua_mario.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GroundPoundFlip implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("ground_pound_flip");

	private static final float FLIP_DURATION = 5;

	public static AnimationDefinition makeAnimation(float flipDuration) {
		return makeAnimation(animationTime -> Math.min(animationTime / flipDuration, 1));
	}
	public static AnimationDefinition makeAnimation(Float2FloatFunction progressCalculator) {
		return AnimationDefinition.of(
				AnimationFlag.NO_SWING_LIMBS,
				(arrangement, data, animationTime, helper) -> {
					float progress = progressCalculator.get(animationTime);
					arrangement.y = progress * -8;
					arrangement.pitch = progress * -360;
				},
				(posture, data, animationTime, helper) -> {
					float progress = progressCalculator.get(animationTime);
					helper.symmetricallyAnimate(posture, posture.RIGHT_ARM, arrangement -> {
						arrangement.addPos(0, progress * 1, progress * 2.5F);
						arrangement.addAngles(
								progress * -67.75F,
								0,
								MathHelper.lerp(progress, 20, -20)
						);
					});

					helper.symmetricallyAnimate(posture, posture.RIGHT_LEG, arrangement -> arrangement.addAngles(
							progress * -90,
							progress * 16.75F,
							0
					));
				}
		);
	}

	@Override public @Nullable AnimationDefinition defineAnimation() {
		return makeAnimation(FLIP_DURATION);
	}

	public static CameraAnimationSet makeCameraAnimations(float flipAnimationDuration) {
		return new CameraAnimationSet(
				MarioQuaMario.CONFIG::getGroundPoundCameraAnim,
				new CameraAnimation(
						new CameraProgressHandler((data, ticksPassed) -> Easing.mixedEase(Easing.SINE_IN_OUT, Easing.SINE_IN_OUT, Math.min(ticksPassed / flipAnimationDuration, 1))),
						(data, arrangement, progress) -> arrangement.pitch += progress * 360
				),
				new CameraAnimation(
						new CameraProgressHandler((data, ticksPassed) -> Easing.EXPO_IN_OUT.ease(Math.min(ticksPassed / flipAnimationDuration, 1))),
						(data, arrangement, progress) -> arrangement.pitch += progress * 360
				),
				null
		);
	}
	@Override public @Nullable CameraAnimationSet defineCameraAnimations(AnimationHelper helper) {
		return makeCameraAnimations(FLIP_DURATION + 2.5F);
	}

	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	public static final StateAttachmentKey<MutableInt> FLIP_TIMER =
			StateAttachmentKey.action(() -> new MutableInt());
	public static final StateAttachmentKey<Float> STORED_FALL_DISTANCE =
			StateAttachmentKey.action(data -> data.getPlayer().fallDistance);

	@Override
	public void onEnter(CfaData data, ImmutableSet.Builder<StateAttachmentKey<?>> attachmentsToKeep) {
		attachmentsToKeep.add(FLIP_TIMER, STORED_FALL_DISTANCE);
		data.get(STORED_FALL_DISTANCE); // Ensure it's stored
	}

	@Override public void travelHook(CfaTravelData data, AirborneActionHelper helper) {
		data.get(FLIP_TIMER).increment();
		data.setYVel(0.15);
	}

	public static final ActionTransitionDetails GROUND_POUND = new ActionTransitionDetails(
			ID,
			data -> data.getInputs().DUCK.isPressed(),
			EvaluatorEnvironment.CLIENT_ONLY,
			SizeChangeBehavior.AUTOMATIC,
			data -> data.setVelocity(Vec3d.ZERO),
			(data, isSelf, seed) -> data.playSound(MarioSFX.GROUND_POUND_FLIP, seed)
	);

	public static ActionTransitionDetails makeDropTransition(Identifier targetAction, float flipDuration) {
		return new ActionTransitionDetails(
				targetAction,
				data -> data.get(FLIP_TIMER).intValue() >= flipDuration,
				EvaluatorEnvironment.COMMON,
				SizeChangeBehavior.AUTOMATIC,
				data -> {
					data.setYVel(GroundPoundDrop.GROUND_POUND_VEL.get(data));
					data.getInputs().JUMP.isPressed(); // Unbuffer jump to make Ground Pound stalling harder
					data.getPlayer().fallDistance = data.get(STORED_FALL_DISTANCE) * 0.6F;
				},
				(data, isSelf, seed) -> {
					// Sound is not played here, it's in the tick method of the target action!
					data.getPlayer().fallDistance = data.get(STORED_FALL_DISTANCE) * 0.6F;
				}
		);
	}

	@Override
	public void accumulateBasicTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {
		builder.add(makeDropTransition(GroundPoundDrop.ID, FLIP_DURATION));
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {
		builder.add(Submerged.SUBMERGE.variate(AquaticPoundFlip.ID, null));
	}
}