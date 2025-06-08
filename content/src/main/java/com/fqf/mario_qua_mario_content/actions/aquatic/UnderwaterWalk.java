package com.fqf.mario_qua_mario_content.actions.aquatic;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_content.actions.airborne.Fall;
import com.fqf.mario_qua_mario_content.actions.grounded.DuckWaddle;
import com.fqf.mario_qua_mario_content.actions.grounded.SubWalk;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class UnderwaterWalk implements AquaticActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("underwater_walk");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	private static final float LEG_HEIGHT_OFFSET = -1.8F;
	private static LimbAnimation makeArmAnimation(int factor) {
		return new LimbAnimation(true, (data, arrangement, progress) -> {
			arrangement.roll *= -1;
			arrangement.yaw -= arrangement.pitch * factor * 0.5F;
			arrangement.roll -= arrangement.pitch * factor * 0.5F;
			arrangement.pitch *= 1;
			arrangement.addAngles(
					-50,
					factor * -10,
					factor * 60
			);
		});
	}
	private static LimbAnimation makeLegAnimation(int factor) {
		return new LimbAnimation(true, (data, arrangement, progress) -> {
			arrangement.pitch *= 0.5F;
			arrangement.addPos(
					factor * -0.4F,
					LEG_HEIGHT_OFFSET,
					-0.9F
			);
			arrangement.addAngles(
					17.5F,
					0,
					factor * -1.9F
			);
		});
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return new PlayermodelAnimation(
				null, null,
				new EntireBodyAnimation(0.5F, true,
						(data, arrangement, progress) -> arrangement.y += LEG_HEIGHT_OFFSET),
				null, new BodyPartAnimation((data, arrangement, progress) -> arrangement.pitch += 15),
				makeArmAnimation(1), makeArmAnimation(-1),
				makeLegAnimation(1), makeLegAnimation(-1),
				null
		);
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return null;
	}

	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.NOT_SLIDING;
	}

	@Override public @NotNull SneakingRule getSneakingRule() {
		return SneakingRule.ALLOW;
	}
	@Override public @NotNull SprintingRule getSprintingRule() {
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable BumpType getBumpType() {
		return null;
	}
	@Override public @Nullable Identifier getStompTypeID() {
		return null;
	}

	public static CharaStat REDUCED_FORWARD_ACCEL = Submerged.FORWARD_SWIM_ACCEL.variate(0.475);
	public static CharaStat REDUCED_FORWARD_SPEED = Submerged.FORWARD_SWIM_SPEED.variate(0.475);
	public static CharaStat REDUCED_BACKWARD_ACCEL = Submerged.BACKWARD_SWIM_ACCEL.variate(0.475);
	public static CharaStat REDUCED_BACKWARD_SPEED = Submerged.BACKWARD_SWIM_SPEED.variate(0.475);
	public static CharaStat REDUCED_STRAFE_ACCEL = Submerged.STRAFE_SWIM_ACCEL.variate(0.475);
	public static CharaStat REDUCED_STRAFE_SPEED = Submerged.STRAFE_SWIM_SPEED.variate(0.475);

	public static CharaStat REDUCED_REDIRECTION = Submerged.SWIM_REDIRECTION.variate(0.7);

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return null;
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, AquaticActionHelper helper) {
		Submerged.waterMove(data, helper);
		helper.aquaticAccel(
				data,
				REDUCED_FORWARD_ACCEL, REDUCED_FORWARD_SPEED,
				REDUCED_BACKWARD_ACCEL, REDUCED_BACKWARD_SPEED,
				REDUCED_STRAFE_ACCEL, REDUCED_STRAFE_SPEED,
				data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				REDUCED_REDIRECTION
		);
	}

	public static final TransitionDefinition SUBMERGE = new TransitionDefinition(
			ID,
			data -> data.getImmersionPercent() > 0.5,
			EvaluatorEnvironment.COMMON
	);

	public static final TransitionDefinition EXIT_WATER = new TransitionDefinition(
			SubWalk.ID,
			data -> data.getImmersionPercent() <= 0.3,
			EvaluatorEnvironment.COMMON
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AquaticActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AquaticActionHelper helper) {
		return List.of(
				Swim.SWIM,
				DuckWaddle.DUCK.variate(UnderwaterDuck.ID, null)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AquaticActionHelper helper) {
		return List.of(
				EXIT_WATER,
				Fall.FALL.variate(Submerged.ID,
						data -> Fall.FALL.evaluator().shouldTransition(data) && SUBMERGE.evaluator().shouldTransition(data))
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}