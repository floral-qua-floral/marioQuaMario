package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.cfadata.CfaAuthoritativeData;
import com.fqf.charaformact_api.cfadata.CfaClientData;
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
import com.fqf.charaformact_api.util.Easing;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.aquatic.AquaticPoundFlip;
import com.fqf.mario_qua_mario.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.MarioSFX;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class GroundPoundFlip implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("ground_pound_flip");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

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

	@Override public @Nullable AnimationDefinition getAnimation() {
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

	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return makeCameraAnimations(FLIP_DURATION + 2.5F);
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

	@Override public @Nullable BappingRule getBappingRule() {
		return null;
	}
	@Override public @Nullable Identifier getCollisionAttackTypeID() {
		return null;
	}

	public static class FlipTimerVars extends ActionTimerVars {
		private final float STORED_FALL_DISTANCE;
		public FlipTimerVars(CfaData data) {
			this.STORED_FALL_DISTANCE = data.getPlayer().fallDistance;

			FlipTimerVars existingVars = data.retrieveStateData(FlipTimerVars.class);
			if(existingVars != null) this.actionTimer = existingVars.actionTimer;
		}
	}
	@Override public @Nullable Object provideStateData(CfaData data) {
		return new FlipTimerVars(data);
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {

	}
	@Override public void serverTick(CfaAuthoritativeData data) {

	}
	@Override public void travelHook(CfaTravelData data, AirborneActionHelper helper) {
		data.retrieveStateData(FlipTimerVars.class).actionTimer++;
		data.setYVel(0.15);
	}

	public static final TransitionDefinition GROUND_POUND = new TransitionDefinition(
			ID,
			data -> data.getInputs().DUCK.isPressed(),
			EvaluatorEnvironment.CLIENT_ONLY,
			data -> {
				data.setVelocity(Vec3d.ZERO);
			},
			(data, isSelf, seed) -> data.playSound(MarioSFX.GROUND_POUND_FLIP, seed)
	);

	public static TransitionDefinition makeDropTransition(Identifier targetAction, float flipDuration, SoundEvent sfx) {
		return new TransitionDefinition(
				targetAction,
				data -> data.retrieveStateData(FlipTimerVars.class).actionTimer >= flipDuration,
				EvaluatorEnvironment.COMMON,
				data -> {
					data.setYVel(GroundPoundDrop.GROUND_POUND_VEL.get(data));
					data.getInputs().JUMP.isPressed(); // Unbuffer jump to make Ground Pound stalling harder
					data.getPlayer().fallDistance = data.retrieveStateData(FlipTimerVars.class).STORED_FALL_DISTANCE * 0.6F;
				},
				(data, isSelf, seed) -> {
					data.storeSound(data.playSound(sfx, seed));
					data.getPlayer().fallDistance = data.retrieveStateData(FlipTimerVars.class).STORED_FALL_DISTANCE * 0.6F;
				}
		);
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AirborneActionHelper helper) {
		return List.of(
				makeDropTransition(GroundPoundDrop.ID, FLIP_DURATION, MarioSFX.GROUND_POUND_DROP)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper) {
		return List.of(
				Submerged.SUBMERGE.variate(AquaticPoundFlip.ID, null)
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}