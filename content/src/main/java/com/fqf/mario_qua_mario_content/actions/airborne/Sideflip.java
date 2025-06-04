package com.fqf.mario_qua_mario_content.actions.airborne;

import com.fqf.mario_qua_mario_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.TransitionInjectionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimation;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraProgressHandler;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_api.util.Easing;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.util.ActionTimerVars;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario_api.util.StatCategory.*;

public class Sideflip extends Backflip implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("sideflip");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null,
				new ProgressHandler((data, ticksPassed) -> Easing.LINEAR.ease(Math.min(ticksPassed / 20F, 1))),
				new EntireBodyAnimation(0.5F, false, (data, arrangement, progress) -> {
					arrangement.yaw += MathHelper.lerp(progress, -136.5F, 0);
					arrangement.roll += Easing.mixedEase(Easing.LINEAR, Easing.QUAD_IN_OUT, progress, -342.5F, 0);
					arrangement.y -= MathHelper.lerp(progress, 4F, 0);
				}),
				new BodyPartAnimation((data, arrangement, progress) -> {
					arrangement.yaw += MathHelper.lerp(Math.min(progress * 4, 1), 42.5F, 0F);
					arrangement.roll += MathHelper.lerp(Math.min(progress * 4, 1), -17.5F, 0F);
				}),
				null,
				new LimbAnimation(false, (data, arrangement, progress) -> {
					float fourProgress = progress * 4;
					arrangement.addAngles(
							helper.interpolateKeyframes(fourProgress, -32, -85, 10, 24, 12),
							helper.interpolateKeyframes(fourProgress, -35, 0, 47.5F, 20, 0),
							helper.interpolateKeyframes(fourProgress, 80, 90, 130, 130, 115)
					);
				}),
				new LimbAnimation(false, (data, arrangement, progress) -> {
					float fourProgress = progress * 4;
					arrangement.addAngles(
							helper.interpolateKeyframes(fourProgress, -45, 30, 0, 0, -4),
							helper.interpolateKeyframes(fourProgress, 0, 20, -17.5F, -35, 0),
							helper.interpolateKeyframes(fourProgress, -30, -90, -115, -107.5F, -137.5F)
					);
				}),

				new LimbAnimation(false, (data, arrangement, progress) -> {
					float fourProgress = progress * 4;
					arrangement.addPos(
							0,
							helper.interpolateKeyframes(fourProgress, 0, 0, -4.1F, -1.2633F, 0),
							helper.interpolateKeyframes(fourProgress, 0, 0, -4.0F, -3.75F, 0)
					);
					arrangement.addAngles(
							helper.interpolateKeyframes(fourProgress, -57.5F, 40, 26, 38.5F, 20),
							helper.interpolateKeyframes(fourProgress, 45, 0, -7, -5, 0),
							helper.interpolateKeyframes(fourProgress, -20, -12.5F, 8, 10, 0)
					);
				}),
				new LimbAnimation(false, (data, arrangement, progress) -> {
					float fourProgress = progress * 4;
					arrangement.addPos(
							0,
							helper.interpolateKeyframes(fourProgress, -4.1F, -1.5F, -0.8F, -1.85F, -3.9F),
							helper.interpolateKeyframes(fourProgress, -3.9F, -2.1F, -0.91F, -2.3F, -1.9F)
					);
					arrangement.addAngles(
							helper.interpolateKeyframes(fourProgress, 5, 20, 5, 4, 0),
							helper.interpolateKeyframes(fourProgress, 15, 0, 0, 5, 0),
							helper.interpolateKeyframes(fourProgress, 0, -22.5F, -15, 0, 0)
					);
				}),
				null
		);
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return new CameraAnimationSet(
				MarioQuaMarioContent.CONFIG::getSideflipCameraAnim,
				new CameraAnimation(
						new CameraProgressHandler((data, ticksPassed) -> Math.min(ticksPassed / 16, 1)),
						(data, arrangement, progress) -> {
							arrangement.yaw += MathHelper.lerp(Easing.SINE_IN_OUT.ease(progress), 180, 0);
							arrangement.roll += Easing.QUAD_IN_OUT.ease(progress) * -360;
						}
				),
				new CameraAnimation(
						new CameraProgressHandler((data, ticksPassed) -> Easing.QUAD_IN_OUT.ease(Math.min(ticksPassed / 14, 1))),
						(data, arrangement, progress) -> {
							arrangement.pitch = MathHelper.lerp(progress, -180 - arrangement.pitch, arrangement.pitch);
							arrangement.roll += MathHelper.lerp(progress, 180, 0);
						}
				),
				new CameraAnimation(
						new CameraProgressHandler((data, ticksPassed) -> Easing.SINE_IN_OUT.ease(Math.min(ticksPassed / 10, 1))),
						(data, arrangement, progress) -> arrangement.yaw += MathHelper.lerp(progress, 180, 0)
				)
		);
	}

	public static CharaStat SIDEFLIP_VEL = new CharaStat(1.065, JUMP_VELOCITY);
	public static CharaStat SIDEFLIP_BACKWARDS_SPEED = new CharaStat(-0.375, DRIFTING, BACKWARD, SPEED);
	public static CharaStat SIDEFLIP_THRESHOLD = new CharaStat(0.2, WALKING, FRICTION, THRESHOLD);

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return null;
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, Fall.FALL_ACCEL, JUMP_GRAVITY, Fall.FALL_SPEED);
		if(data.getYVel() < 0.1) Fall.drift(data, helper);
	}

	@Override protected double getJumpCapThreshold() {
		return 0.65;
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}