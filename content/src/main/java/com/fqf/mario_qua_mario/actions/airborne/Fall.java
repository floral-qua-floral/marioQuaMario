package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.piecemeal.PiecemealPlayermodelAnimation;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.cfadata.*;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario.actions.grounded.SubWalk;
import com.fqf.mario_qua_mario.actions.wallbound.WallSlide;
import com.fqf.mario_qua_mario.collision_attacks.JumpStomp;
import com.fqf.mario_qua_mario.util.ClimbTransitions;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class Fall implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("fall");
	@Override public @NotNull Identifier getID() {
	    return ID;
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
		return SprintingRule.IF_ALREADY_SPRINTING;
	}

	@Override public @Nullable BappingRule getBappingRule() {
		return BappingRule.FALLING;
	}
	@Override public @Nullable Identifier getCollisionAttackTypeID() {
		return JumpStomp.ID;
	}

	public static final CfaStat FALL_ACCEL = new CfaStat(-0.115, NORMAL_GRAVITY);
	public static final CfaStat FALL_SPEED = new CfaStat(-3.25, TERMINAL_VELOCITY);

	public static final CfaStat FORWARD_DRIFT_ACCEL = new CfaStat(0.045, DRIFTING, FORWARD, ACCELERATION);
	public static final CfaStat FORWARD_DRIFT_SPEED = new CfaStat(0.275, DRIFTING, FORWARD, SPEED);

	public static final CfaStat BACKWARD_DRIFT_ACCEL = new CfaStat(0.055, DRIFTING, BACKWARD, ACCELERATION);
	public static final CfaStat BACKWARD_DRIFT_SPEED = new CfaStat(0.2, DRIFTING, BACKWARD, SPEED);

	public static final CfaStat STRAFE_DRIFT_ACCEL = new CfaStat(0.065, DRIFTING, STRAFE, ACCELERATION);
	public static final CfaStat STRAFE_DRIFT_SPEED = new CfaStat(0.25, DRIFTING, STRAFE, SPEED);

	public static final CfaStat DRIFT_REDIRECTION = new CfaStat(6.0, DRIFTING, REDIRECTION);

	public static void drift(CfaTravelData data, AirborneActionHelper helper) {
		helper.airborneAccel(
				data,
				FORWARD_DRIFT_ACCEL, FORWARD_DRIFT_SPEED,
				BACKWARD_DRIFT_ACCEL, BACKWARD_DRIFT_SPEED,
				STRAFE_DRIFT_ACCEL, STRAFE_DRIFT_SPEED,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(), DRIFT_REDIRECTION
		);
	}

	public static final TransitionDefinition FALL = new TransitionDefinition(
			Fall.ID,
			data -> !data.getPlayer().isOnGround(),
			EvaluatorEnvironment.CLIENT_ONLY
	);

	@Override public @Nullable Object provideStateData(CfaData data) {
		return null;
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {

	}
	@Override public void serverTick(CfaAuthoritativeData data) {
		
	}
	@Override public void travelHook(CfaTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, FALL_ACCEL, null, FALL_SPEED);
		drift(data, helper);
	}

	public static final TransitionDefinition LANDING = new TransitionDefinition(
			SubWalk.ID,
			data -> data.getPlayer().isOnGround(),
			EvaluatorEnvironment.COMMON
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
				this.getLandingTransition(),
				ClimbTransitions.CLIMB_NON_SOLID_DIRECTIONAL,
				ClimbTransitions.CLIMB_NON_SOLID_NON_DIRECTIONAL,
				ClimbTransitions.CLIMB_SOLID,
				WallSlide.WALL_SLIDE
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
