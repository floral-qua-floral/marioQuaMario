package com.fqf.mario_qua_mario_content.actions.aquatic;

import com.fqf.mario_qua_mario_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.airborne.Fall;
import com.fqf.mario_qua_mario_content.actions.airborne.GroundPoundFlip;
import com.fqf.mario_qua_mario_content.actions.airborne.SpecialFall;
import com.fqf.mario_qua_mario_content.util.MarioContentSFX;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario_api.util.StatCategory.*;

public class Submerged implements AquaticActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("submerged");
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
	@Override public @Nullable Identifier getStompTypeID() {
		return null;
	}

	public static final CharaStat FALL_ACCEL = new CharaStat(-0.035, AQUATIC_GRAVITY);
	public static final CharaStat FALL_SPEED = new CharaStat(-0.675, AQUATIC_TERMINAL_VELOCITY);

	public static final CharaStat DRAG = new CharaStat(0.11, WATER_DRAG);
	public static final CharaStat DRAG_MIN = new CharaStat(0.01, WATER_DRAG);

	public static final CharaStat FORWARD_SWIM_ACCEL = new CharaStat(0.025, SWIMMING, FORWARD, ACCELERATION);
	public static final CharaStat FORWARD_SWIM_SPEED = new CharaStat(0.25, SWIMMING, FORWARD, SPEED);

	public static final CharaStat BACKWARD_SWIM_ACCEL = new CharaStat(0.035, SWIMMING, BACKWARD, ACCELERATION);
	public static final CharaStat BACKWARD_SWIM_SPEED = new CharaStat(0.2, SWIMMING, BACKWARD, SPEED);

	public static final CharaStat STRAFE_SWIM_ACCEL = new CharaStat(0.025, SWIMMING, STRAFE, ACCELERATION);
	public static final CharaStat STRAFE_SWIM_SPEED = new CharaStat(0.25, SWIMMING, STRAFE, SPEED);

	public static final CharaStat SWIM_REDIRECTION = new CharaStat(2.0, SWIMMING, REDIRECTION);

	public static void waterMove(IMarioTravelData data, AquaticActionHelper helper) {
		helper.applyGravity(data, FALL_ACCEL, FALL_SPEED);
		helper.applyWaterDrag(data, DRAG, DRAG_MIN);
	}

	public static void drift(IMarioTravelData data, AquaticActionHelper helper) {
		helper.aquaticAccel(data,
				FORWARD_SWIM_ACCEL, FORWARD_SWIM_SPEED,
				BACKWARD_SWIM_ACCEL, BACKWARD_SWIM_SPEED,
				STRAFE_SWIM_ACCEL, STRAFE_SWIM_SPEED,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				SWIM_REDIRECTION
		);
	}

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return null;
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, AquaticActionHelper helper) {
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
				GroundPoundFlip.GROUND_POUND.variate(
						AquaticPoundFlip.ID,
						null,
						null,
						null,
						(data, isSelf, seed) -> data.playSound(MarioContentSFX.AQUATIC_GROUND_POUND_FLIP, seed)
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