package com.fqf.mario_qua_mario.actions.aquatic;

import com.fqf.mario_qua_mario.MarioQuaMarioContent;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.actions.airborne.SpecialFall;
import com.fqf.mario_qua_mario.definitions.states.actions.AquaticActionDefinition;
import com.fqf.mario_qua_mario.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.CameraAnimationSet;
import com.fqf.mario_qua_mario.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario.mariodata.IMarioData;
import com.fqf.mario_qua_mario.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.CharaStat;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario.util.StatCategory.*;

public class Submerged implements AquaticActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("submerged");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return null;
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
		return SprintingRule.PROHIBIT;
	}

	@Override public @Nullable BumpType getBumpType() {
		return null;
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
		return new ActionTimerVars();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, AquaticActionHelper helper) {
		data.getVars(ActionTimerVars.class).actionTimer++;
		helper.applyGravity(data, FALL_ACCEL, FALL_SPEED);
		helper.applyWaterDrag(data, DRAG, DRAG_MIN);
		drift(data, helper);
	}

	public static final TransitionDefinition SUBMERGE = new TransitionDefinition(
			ID,
			data -> data.getMario().getFluidHeight(FluidTags.WATER) > 0,
			EvaluatorEnvironment.COMMON,
			data -> data.setYVel(data.getYVel() * 0.225),
			(data, isSelf, seed) -> {}
	);

	public static final TransitionDefinition EXIT_WATER = new TransitionDefinition(
			SpecialFall.ID,
			data -> data.getMario().getFluidHeight(FluidTags.WATER) <= 0,
			EvaluatorEnvironment.COMMON
	);

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AquaticActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AquaticActionHelper helper) {
		return List.of(
				Swim.SWIM
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AquaticActionHelper helper) {
		return List.of(
			EXIT_WATER
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}