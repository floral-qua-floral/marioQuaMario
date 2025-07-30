package com.fqf.mario_qua_mario_content.actions.grounded;

import com.fqf.mario_qua_mario_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.airborne.BonkAir;
import com.fqf.mario_qua_mario_content.actions.airborne.Fall;
import com.fqf.mario_qua_mario_content.actions.aquatic.UnderwaterWalk;
import com.fqf.mario_qua_mario_content.util.ActionTimerVars;
import com.fqf.mario_qua_mario_content.util.StandUpWithKneeAnimation;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario_api.util.StatCategory.*;

public class BonkGroundBackward implements GroundedActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("bonk_ground_backward");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	public static PlayermodelAnimation makeBonkStandupAnimation(
			AnimationHelper helper, ProgressHandler.ProgressCalculator progressCalculator
	) {
		return StandUpWithKneeAnimation.makeAnimation(
				helper, progressCalculator,
				1.75F, 10,
				22.5F, -20, 0, 2,
				-90, 5, 1.5F,
				-79, 10, -1.55F, 3
		);
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return makeBonkStandupAnimation(helper, (data, ticksPassed) -> data.getVars(ActionTimerVars.class).actionTimer / (float) STANDUP_TICKS);
	}

	@Override public @Nullable CameraAnimationSet getCameraAnimations(AnimationHelper helper) {
		return null;
	}
	@Override public @NotNull SlidingStatus getSlidingStatus() {
		return SlidingStatus.SLIDING;
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

	public static final CharaStat BONK_DRAG = new CharaStat(0.185, RUNNING, DRAG);
	public static final CharaStat BONK_DRAG_MIN = new CharaStat(0.045, RUNNING, DRAG);

	public static final int STANDUP_TICKS = 8;

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return new ActionTimerVars();
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public void travelHook(IMarioTravelData data, GroundedActionHelper helper) {
		if(data.getHorizVelSquared() == 0) data.getVars(ActionTimerVars.class).actionTimer++;
		else data.getVars(ActionTimerVars.class).actionTimer = 0;
		helper.applyDrag(
				data, BONK_DRAG, BONK_DRAG_MIN,
				-data.getInputs().getForwardInput(), data.getInputs().getStrafeInput(),
				CharaStat.ZERO
		);
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(GroundedActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						SubWalk.ID,
						data -> data.getVars(ActionTimerVars.class).actionTimer > STANDUP_TICKS,
						EvaluatorEnvironment.CLIENT_ONLY
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(GroundedActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(GroundedActionHelper helper) {
		return List.of(
				Fall.FALL.variate(
						BonkAir.ID,
						data -> Fall.FALL.evaluator().shouldTransition(data) && data.getVars(ActionTimerVars.class).actionTimer == 0,
						EvaluatorEnvironment.CLIENT_ONLY,
						null,
						null
				),
				Fall.FALL,
				UnderwaterWalk.SUBMERGE
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override
	public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
