package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario.definitions.states.actions.AirborneActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.CameraAnimationSet;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.stomp_types.JumpStomp;
import com.fqf.mario_qua_mario.util.CharaStat;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario.util.StatCategory.*;

public class Fall implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("fall");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null, null,
				null,
				null, null,
				null, null,
				null, null,
				null
		);
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations() {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.IF_ALREADY_SPRINTING;
	}

	@Override public @Nullable BumpType getBumpType() {
		return null;
	}
	@Override public @Nullable Identifier getStompTypeID() {
		return JumpStomp.ID;
	}

	public static final CharaStat FALL_ACCEL = new CharaStat(-0.115, NORMAL_GRAVITY);
	public static final CharaStat FALL_SPEED = new CharaStat(-3.25, TERMINAL_VELOCITY);

	public static final CharaStat FORWARD_DRIFT_ACCEL = new CharaStat(0.045, DRIFTING, FORWARD, ACCELERATION);
	public static final CharaStat FORWARD_DRIFT_SPEED = new CharaStat(0.275, DRIFTING, FORWARD, SPEED);

	public static final CharaStat BACKWARD_DRIFT_ACCEL = new CharaStat(0.055, DRIFTING, BACKWARD, ACCELERATION);
	public static final CharaStat BACKWARD_DRIFT_SPEED = new CharaStat(0.2, DRIFTING, BACKWARD, SPEED);

	public static final CharaStat STRAFE_DRIFT_ACCEL = new CharaStat(0.065, DRIFTING, STRAFE, ACCELERATION);
	public static final CharaStat STRAFE_DRIFT_SPEED = new CharaStat(0.25, DRIFTING, STRAFE, SPEED);

	public static final CharaStat DRIFT_REDIRECTION = new CharaStat(6.0, DRIFTING, REDIRECTION);

	public static void drift(IMarioTravelData data, AirborneActionHelper helper) {
		helper.airborneAccel(
				data,
				FORWARD_DRIFT_ACCEL, FORWARD_DRIFT_SPEED,
				BACKWARD_DRIFT_ACCEL, BACKWARD_DRIFT_SPEED,
				STRAFE_DRIFT_ACCEL, STRAFE_DRIFT_SPEED,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(), DRIFT_REDIRECTION
		);
	}

	public static final TransitionDefinition FALL = new TransitionDefinition(
			MarioQuaMarioContent.makeID("fall"),
			data -> !data.getMario().isOnGround(),
			EvaluatorEnvironment.CLIENT_ONLY
	);

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return null;
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {
		
	}
	@Override public void travelHook(IMarioTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, FALL_ACCEL, null, FALL_SPEED);
		drift(data, helper);
	}

	public static final TransitionDefinition LANDING = new TransitionDefinition(
			MarioQuaMarioContent.makeID("sub_walk"),
			data -> data.getMario().isOnGround(),
			EvaluatorEnvironment.CLIENT_CHECKED
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AirborneActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AirborneActionHelper helper) {
		return List.of(
				GroundPoundFlip.GROUND_POUND
		);
	}
	protected TransitionDefinition getLandingTransition() {
		return LANDING;
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AirborneActionHelper helper) {
		return List.of(
				Submerged.SUBMERGE,
				this.getLandingTransition()
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
