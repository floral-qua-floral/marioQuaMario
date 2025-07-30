package com.fqf.mario_qua_mario_content.actions.grounded;

import com.fqf.mario_qua_mario_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.airborne.Fall;
import com.fqf.mario_qua_mario_content.actions.airborne.GroundPoundDrop;
import com.fqf.mario_qua_mario_content.actions.aquatic.AquaticPoundLand;
import com.fqf.mario_qua_mario_content.actions.aquatic.UnderwaterWalk;
import com.fqf.mario_qua_mario_content.util.ActionTimerVars;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class GroundPoundLand implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("ground_pound_land");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	private static final float STANDUP_TICKS = 10;

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return BonkGroundBackward.makeBonkStandupAnimation(helper, (data, ticksPassed) -> ticksPassed / STANDUP_TICKS);
	}
	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.SLIDING_SILENT;
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
	@Override public @Nullable Identifier getStompTypeID() {
		return null;
	}

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new ActionTimerVars();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, GroundedActionHelper helper) {
		data.setForwardStrafeVel(0, 0);
		ActionTimerVars.get(data).actionTimer++;
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						SubWalk.ID,
						data -> ActionTimerVars.get(data).actionTimer > STANDUP_TICKS,
						EvaluatorEnvironment.COMMON
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(GroundedActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(GroundedActionHelper helper) {
		return List.of(
				Fall.FALL.variate(
						GroundPoundDrop.ID,
						data -> data.getInputs().DUCK.isHeld() && Fall.FALL.evaluator().shouldTransition(data)
				),
				Fall.FALL,
				UnderwaterWalk.SUBMERGE.variate(AquaticPoundLand.ID, null)
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}