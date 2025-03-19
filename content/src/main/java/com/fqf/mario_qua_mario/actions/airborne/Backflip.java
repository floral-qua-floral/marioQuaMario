package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.Voicelines;
import com.fqf.mario_qua_mario.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.CharaStat;
import com.fqf.mario_qua_mario.util.Easing;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario.util.StatCategory.*;

public class Backflip extends Jump implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("backflip");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	private static LimbAnimation makeArmAnimation(AnimationHelper helper, int factor) {
	    return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.pitch += helper.interpolateKeyframes(progress,
					-70.5F,
					-100,
					-98,
					-90,
					0
			);
			arrangement.yaw += helper.interpolateKeyframes(progress,
					factor * 70,
					factor * 60,
					factor * 106,
					factor * 110,
					factor * (factor == 1 ? -40 : 80)
			);
			arrangement.roll += helper.interpolateKeyframes(progress,
					factor * 90,
					0,
					0,
					0,
					factor * 110
			);
			arrangement.y += helper.interpolateKeyframes(progress, 0, 0, 1, 0);
			arrangement.z += helper.interpolateKeyframes(progress, 0, 0, -1, 0);
	    });
	}
	private static LimbAnimation makeLegAnimation(AnimationHelper helper, int offsetFactor) {
	    return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.pitch += helper.interpolateKeyframes(progress,
					0,
					48,
					22.5F,
					19 + offsetFactor * 21,
					(offsetFactor == 0) ? -12.5F : 9.1F
			);
			arrangement.y += helper.interpolateKeyframes(progress,
					0,
					0,
					0,
					offsetFactor * -1.75F,
					offsetFactor * -4.5F
			);
			arrangement.z += helper.interpolateKeyframes(progress,
					0,
					0,
					0,
					-2 + offsetFactor * -1.8F,
					offsetFactor * -4.25F
			);
	    });
	}
	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
	    return new PlayermodelAnimation(
	            (data, rightArmBusy, leftArmBusy, headRelativeYaw) -> data.getMario().getRandom().nextBoolean(),
				new ProgressHandler(
						(data, ticksPassed) -> helper.sequencedEase(helper.sequencedEase(ticksPassed / 4.4F,
								Easing.LINEAR, Easing.LINEAR, Easing.LINEAR, Easing.LINEAR) / 3, Easing.LINEAR, Easing.LINEAR) * 3
				),
				new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {
					arrangement.pitch += progress * 180;
				}),
	            null,
	            null,
	            makeArmAnimation(helper, 1), makeArmAnimation(helper, -1),
	            makeLegAnimation(helper, 0), makeLegAnimation(helper, 1),
	            null
	    );
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations() {
		return null;
	}

	public static CharaStat BACKFLIP_VEL = new CharaStat(1.065, JUMP_VELOCITY);
	public static CharaStat BACKFLIP_BACKWARDS_SPEED = new CharaStat(-0.16, DRIFTING, BACKWARD, SPEED);

	public static CharaStat REDUCED_FORWARD_ACCEL = Fall.FORWARD_DRIFT_ACCEL.variate(0.5);
	public static CharaStat REDUCED_FORWARD_SPEED = Fall.FORWARD_DRIFT_SPEED.variate(0.5);
	public static CharaStat REDUCED_BACKWARD_ACCEL = Fall.BACKWARD_DRIFT_ACCEL.variate(0.5);
	public static CharaStat REDUCED_BACKWARD_SPEED = Fall.BACKWARD_DRIFT_SPEED.variate(0.5);
	public static CharaStat REDUCED_STRAFE_ACCEL = Fall.STRAFE_DRIFT_ACCEL.variate(0.5);
	public static CharaStat REDUCED_STRAFE_SPEED = Fall.STRAFE_DRIFT_SPEED.variate(0.5);

	public static CharaStat BACKFLIP_REDIRECTION = new CharaStat(0, DRIFTING, REDIRECTION);

	@Override public void travelHook(IMarioTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, Fall.FALL_ACCEL, JUMP_GRAVITY, Fall.FALL_SPEED);
		if(data.getYVel() < 0.1) Fall.drift(data, helper);
		else helper.airborneAccel(
				data,
				REDUCED_FORWARD_ACCEL, REDUCED_FORWARD_SPEED,
				REDUCED_BACKWARD_ACCEL, REDUCED_BACKWARD_SPEED,
				REDUCED_STRAFE_ACCEL, REDUCED_STRAFE_SPEED,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				BACKFLIP_REDIRECTION
		);
	}

	public static TransitionDefinition makeBackflipTransition(GroundedActionDefinition.GroundedActionHelper helper) {
		return new TransitionDefinition(
				ID,
				data ->
						data.getForwardVel() < 0.0 && data.getInputs().getForwardInput() < -0.65 && data.getInputs().JUMP.isPressed(),
				EvaluatorEnvironment.CLIENT_ONLY,
				data -> {
					helper.performJump(data, BACKFLIP_VEL, null);
					data.getInputs().DUCK.isPressed(); // Unbuffer duck to prevent accidental buffered Ground Pound

					double backflipBackwardsVel = Backflip.BACKFLIP_BACKWARDS_SPEED.get(data);
					if(data.getForwardVel() > backflipBackwardsVel)
						data.setForwardStrafeVel(backflipBackwardsVel, 0.0);
				},
				(data, isSelf, seed) -> {
					data.playJumpSound(seed);
					data.voice(Voicelines.BACKFLIP, seed);
				}
		);
	}

	@Override protected double getJumpCapThreshold() {
		return 0.765;
	}
}