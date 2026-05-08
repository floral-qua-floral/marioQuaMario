package com.fqf.mario_qua_mario.actions.aquatic;

import com.fqf.charaformact_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.cfadata.*;
import com.fqf.charaformact_api.cfadata.CfaClientData;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.util.CfaStat;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.GroundPoundFlip;
import com.fqf.mario_qua_mario.actions.airborne.SpecialFall;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.MarioSFX;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.charaformact_api.util.StatCategory.*;

public class Submerged implements AquaticActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("submerged");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	private static LimbAnimation makeArmAnimation(int factor, AnimationHelper helper) {
		return new LimbAnimation(false, (data, arrangement, progress) -> {
			arrangement.roll *= -1;
			float threeProgress = progress * 3;
			arrangement.addAngles(
					helper.interpolateKeyframes(threeProgress,
							-75,
							-75,
							-75,
							-50
					),
					helper.interpolateKeyframes(threeProgress,
							factor * 5,
							factor * 5,
							factor * -165F,
							factor * -40
					),
					helper.interpolateKeyframes(threeProgress,
							factor * 125,
							factor * 125,
							factor * 100,
							factor * 60
					)
			);
		});
	}
	private static LimbAnimation makeLegAnimation(int factor) {
		return new LimbAnimation(true, (data, arrangement, progress) -> {
			arrangement.pitch *= 0.5F;
			arrangement.addPos(
					factor * -0.675F,
					-1.2F,
					-1.8F
			);
			arrangement.addAngles(
					50,
					factor * 6,
					0
			);
		});
	}
	public static PlayermodelAnimation makeAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null,
				null,
				new EntireBodyAnimation(0.5F, true, (data, arrangement, progress) -> {

				}),
				null,
				new BodyPartAnimation((data, arrangement, progress) -> {
					arrangement.pitch += 15;
				}),
				makeArmAnimation(1, helper), makeArmAnimation(-1, helper),
				makeLegAnimation(1), makeLegAnimation(-1),
				null
		);
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return makeAnimation(helper);
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

	@Override public @Nullable BappingRule getBappingRule() {
		return new BappingRule(3, 0);
	}
	@Override public @Nullable Identifier getCollisionAttackTypeID() {
		return null;
	}

	public static final CfaStat FALL_ACCEL = new CfaStat(-0.035, AQUATIC_GRAVITY);
	public static final CfaStat FALL_SPEED = new CfaStat(-0.675, AQUATIC_TERMINAL_VELOCITY);

	public static final CfaStat DRAG = new CfaStat(0.11, WATER_DRAG);
	public static final CfaStat DRAG_MIN = new CfaStat(0.01, WATER_DRAG);

	public static final CfaStat FORWARD_SWIM_ACCEL = new CfaStat(0.025, SWIMMING, FORWARD, ACCELERATION);
	public static final CfaStat FORWARD_SWIM_SPEED = new CfaStat(0.25, SWIMMING, FORWARD, SPEED);

	public static final CfaStat BACKWARD_SWIM_ACCEL = new CfaStat(0.035, SWIMMING, BACKWARD, ACCELERATION);
	public static final CfaStat BACKWARD_SWIM_SPEED = new CfaStat(0.2, SWIMMING, BACKWARD, SPEED);

	public static final CfaStat STRAFE_SWIM_ACCEL = new CfaStat(0.025, SWIMMING, STRAFE, ACCELERATION);
	public static final CfaStat STRAFE_SWIM_SPEED = new CfaStat(0.25, SWIMMING, STRAFE, SPEED);

	public static final CfaStat SWIM_REDIRECTION = new CfaStat(2.0, SWIMMING, REDIRECTION);

	public static void waterMove(CfaTravelData data, AquaticActionHelper helper) {
		helper.applyGravity(data, FALL_ACCEL, FALL_SPEED);
		helper.applyWaterDrag(data, DRAG, DRAG_MIN);
	}

	public static void drift(CfaTravelData data, AquaticActionHelper helper) {
		helper.aquaticAccel(data,
				FORWARD_SWIM_ACCEL, FORWARD_SWIM_SPEED,
				BACKWARD_SWIM_ACCEL, BACKWARD_SWIM_SPEED,
				STRAFE_SWIM_ACCEL, STRAFE_SWIM_SPEED,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				SWIM_REDIRECTION
		);
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new ActionTimerVars();
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {
		data.retrieveStateData(ActionTimerVars.class).actionTimer++;
	}
	@Override public void serverTick(CfaAuthoritativeData data) {
		data.retrieveStateData(ActionTimerVars.class).actionTimer++;
	}
	@Override public void travelHook(CfaTravelData data, AquaticActionHelper helper) {
		waterMove(data, helper);
		drift(data, helper);
	}

	public static final TransitionDefinition SUBMERGE = UnderwaterWalk.SUBMERGE.variate(
			ID,
			null,
			null,
			data -> data.setYVel(data.getYVel() * 0.225),
			null
	);

	public static final TransitionDefinition EXIT_WATER = new TransitionDefinition(
			SpecialFall.ID,
			data -> data.getImmersionPercent() <= 0.3,
			EvaluatorEnvironment.COMMON
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AquaticActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AquaticActionHelper helper) {
		return List.of(
				Swim.SWIM,
				AquaticPoundFlip.AQUATIC_GROUND_POUND,
				new TransitionDefinition(
						Paddle.ID,
						data -> {
							ActionTimerVars vars = data.retrieveStateData(ActionTimerVars.class);
							return (vars == null || vars.actionTimer > 7) && data.getInputs().JUMP.isHeld() && data.getForwardVel() > -0.1;
						},
						EvaluatorEnvironment.CLIENT_ONLY,
						data -> {
							data.setYVel(Math.max(Paddle.PADDLE_FALL_SPEED.get(data), data.getYVel()));
						},
						null
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AquaticActionHelper helper) {
		return List.of(
			EXIT_WATER,
			Fall.LANDING.variate(UnderwaterWalk.ID, null)
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}