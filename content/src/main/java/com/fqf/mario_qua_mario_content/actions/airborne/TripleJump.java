package com.fqf.mario_qua_mario_content.actions.airborne;

import com.fqf.mario_qua_mario_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.ActionCategory;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimation;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraProgressHandler;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_api.util.Easing;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.Voicelines;
import com.fqf.mario_qua_mario_content.util.MarioVars;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

import static com.fqf.mario_qua_mario_api.util.StatCategory.*;

public class TripleJump extends Jump implements AirborneActionDefinition {
	@Override public @NotNull Identifier getID() {
		return MarioQuaMarioContent.makeID("triple_jump");
	}

	private static LimbAnimation makeArmAnimation(AnimationHelper helper, int factor) {
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.roll += helper.interpolateKeyframes(progress,
					170 * factor,
					90 * factor,
					0,
					0,
					90 * factor,
					107 * factor
			);
			arrangement.pitch += helper.interpolateKeyframes(progress,
					0,
					0,
					-90,
					-90,
					0,
					0
			);
		});
	}
	private static LimbAnimation makeLegAnimation(AnimationHelper helper, int factor, int offsetFactor) {
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.pitch += helper.interpolateKeyframes(progress,
					0,
					52,
					0,
					-90 + offsetFactor * 20,
					offsetFactor * -38.5F,
					(offsetFactor == 0) ? -9.5F : 9.1F
			);
			arrangement.z += helper.interpolateKeyframes(progress,
					0,
					0,
					0,
					0,
					offsetFactor * -3.25F,
					offsetFactor * -4.25F
			);
			arrangement.y += helper.interpolateKeyframes(progress,
					0,
					0,
					0,
					0,
					offsetFactor * -3.5F,
					offsetFactor * -4.5F
			);
		});
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				(data, rightArmBusy, leftArmBusy, headRelativeYaw) -> data.getMario().getRandom().nextBoolean(),
				new ProgressHandler(
						(data, ticksPassed) -> helper.sequencedEase(helper.sequencedEase(ticksPassed / 5F,
								Easing.LINEAR, Easing.LINEAR, Easing.LINEAR, Easing.LINEAR, Easing.LINEAR) / 3, Easing.LINEAR, Easing.LINEAR) * 3
				),
				new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {
					arrangement.pitch -= Math.min(progress, 4) * 180;
				}),
				new BodyPartAnimation((data, arrangement, progress) -> {

				}),
				new BodyPartAnimation((data, arrangement, progress) -> {

				}),
				makeArmAnimation(helper, 1),
				makeArmAnimation(helper, -1),
				makeLegAnimation(helper, 1, 0),
				makeLegAnimation(helper, -1, 1),
				null
		);
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return new CameraAnimationSet(
				MarioQuaMarioContent.CONFIG::getTripleJumpCameraAnim,
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

	public static final CharaStat TRIPLE_JUMP_VEL = new CharaStat(1.175, JUMP_VELOCITY);
	public static CharaStat TRIPLE_JUMP_SPEED_THRESHOLD = new CharaStat(0.34, RUNNING, FORWARD, THRESHOLD);

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
			this.makeInjection(MarioQuaMarioContent.makeID("jump")),
			this.makeInjection(MarioQuaMarioContent.makeID("p_jump"))
		);
	}
}
