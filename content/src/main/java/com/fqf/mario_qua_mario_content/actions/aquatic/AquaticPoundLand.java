package com.fqf.mario_qua_mario_content.actions.aquatic;

import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_api.util.CharaStat;
import com.fqf.mario_qua_mario_content.actions.airborne.Fall;
import com.fqf.mario_qua_mario_content.actions.airborne.GroundPoundDrop;
import com.fqf.mario_qua_mario_content.actions.grounded.BonkGroundBackward;
import com.fqf.mario_qua_mario_content.util.ActionTimerVars;
import com.fqf.mario_qua_mario_content.util.MarioContentSFX;
import com.fqf.mario_qua_mario_content.util.StandUpWithKneeAnimation;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

import static com.fqf.mario_qua_mario_api.util.StatCategory.*;

public class AquaticPoundLand implements AquaticActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("aquatic_ground_pound_land");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	private static final float AQUATIC_STANDUP_TICKS = 15;

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return StandUpWithKneeAnimation.makeAnimation(
				helper, (data, ticksPassed) -> ticksPassed / AQUATIC_STANDUP_TICKS,
				1.75F, 10,
				22.5F, -20, 0, 2,
				-90, 5, 1.5F,
				-79, 10, -1.55F, 3,

				15, UnderwaterWalk.LEG_HEIGHT_OFFSET,
				-50, -10, 60,
				17.5F, -1.9F, -0.4F, UnderwaterWalk.LEG_HEIGHT_OFFSET, -0.9F
		);
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
	@Override public void travelHook(IMarioTravelData data, AquaticActionHelper helper) {
		Submerged.waterMove(data, helper);
		data.setForwardStrafeVel(0, 0);
		data.getVars(ActionTimerVars.class).actionTimer++;
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AquaticActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						UnderwaterWalk.ID,
						data -> data.getVars(ActionTimerVars.class).actionTimer > AQUATIC_STANDUP_TICKS,
						EvaluatorEnvironment.COMMON
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(AquaticActionHelper helper) {
		return List.of();
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(AquaticActionHelper helper) {
		return List.of(
				Fall.FALL.variate(
						AquaticPoundDrop.ID,
						data -> data.getInputs().DUCK.isHeld() && Fall.FALL.evaluator().shouldTransition(data),
						null,
						data -> data.setYVel(-0.6),
						(data, isSelf, seed) -> data.storeSound(data.playSound(MarioContentSFX.AQUATIC_GROUND_POUND_DROP, seed))
				),
				Fall.FALL.variate(Submerged.ID, null),
				UnderwaterWalk.EXIT_WATER
		);
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}