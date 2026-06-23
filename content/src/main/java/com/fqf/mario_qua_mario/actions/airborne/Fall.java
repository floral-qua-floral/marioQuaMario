package com.fqf.mario_qua_mario.actions.airborne;

import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.charaformact_api.definitions.states.actions.AirborneActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.aquatic.Submerged;
import com.fqf.mario_qua_mario.actions.grounded.SubWalk;
import com.fqf.mario_qua_mario.actions.wallbound.WallSlide;
import com.fqf.mario_qua_mario.collision_attacks.Stomp;
import com.fqf.mario_qua_mario.util.ClimbTransitions;
import com.google.common.collect.ImmutableList;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class Fall implements AirborneActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("fall");

	@Override public @NotNull SneakingRule defineSneakingRule() {
		return SneakingRule.PROHIBIT;
	}
	@Override public @NotNull SprintingRule defineSprintingRule() {
		return SprintingRule.IF_ALREADY_SPRINTING;
	}

	@Override public @Nullable BappingRule defineBappingRule() {
		return BappingRule.FALLING;
	}
	@Override public @Nullable Identifier defineActiveCollisionAttack() {
		return Stomp.ID;
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

	public static final ActionTransitionDetails FALL = new ActionTransitionDetails(
			Fall.ID,
			data -> !data.getPlayer().isOnGround(),
			EvaluatorEnvironment.CLIENT_ONLY
	);

	@Override public void travelHook(CfaTravelData data, AirborneActionHelper helper) {
		helper.applyComplexGravity(data, FALL_ACCEL, null, FALL_SPEED);
		drift(data, helper);
	}

	protected ActionTransitionDetails getLandingTransition() {
		return LANDING;
	}
	public static final ActionTransitionDetails LANDING = new ActionTransitionDetails(
			SubWalk.ID,
			data -> data.getPlayer().isOnGround(),
			EvaluatorEnvironment.COMMON
	);

	@Override
	public void accumulateInputTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {
		builder.add(GroundPoundFlip.GROUND_POUND);
	}

	@Override
	public void accumulateCollisionTransitions(ImmutableList.Builder<ActionTransitionDetails> builder, AirborneActionHelper helper) {
		builder.add(
				Submerged.SUBMERGE,
				this.getLandingTransition(),
				ClimbTransitions.CLIMB_NON_SOLID_DIRECTIONAL,
				ClimbTransitions.CLIMB_NON_SOLID_NON_DIRECTIONAL,
				ClimbTransitions.CLIMB_SOLID,
				WallSlide.WALL_SLIDE,
				LavaBoost.MANUALLY_TRIGGERABLE_LAVA_BOOST
		);
	}
}
