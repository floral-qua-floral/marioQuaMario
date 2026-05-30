package com.fqf.mario_qua_mario.actions.aquatic;

import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.charaformact_api.cfadata.*;
import com.fqf.mario_qua_mario.MarioQuaMario;
import com.fqf.charaformact_api.definitions.states.actions.AquaticActionDefinition;
import com.fqf.charaformact_api.definitions.states.actions.util.*;
import com.fqf.charaformact_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.charaformact_api.cfadata.CfaData;
import com.fqf.charaformact_api.cfadata.CfaTravelData;
import com.fqf.mario_qua_mario.actions.airborne.Fall;
import com.fqf.mario_qua_mario.util.ActionTimerVars;
import com.fqf.mario_qua_mario.util.MarioSFX;
import com.fqf.mario_qua_mario.util.StandUpWithKneeAnimation;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class AquaticPoundLand implements AquaticActionDefinition {
	public static final Identifier ID = MarioQuaMario.makeID("aquatic_ground_pound_land");
	@Override public @NotNull Identifier getID() {
		return ID;
	}

	private static final float AQUATIC_STANDUP_TICKS = 15;

	@Override public @Nullable AnimationDefinition getAnimation() {
		return StandUpWithKneeAnimation.makeAnimation(
				StandUpWithKneeAnimation.makeProgressCalculator(AQUATIC_STANDUP_TICKS),
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
	@Override public @Nullable Identifier getCollisionAttackTypeID() {
		return null;
	}

	@Override public @Nullable Object provideStateData(CfaData data) {
		return new ActionTimerVars();
	}
	@Override public void clientTick(CfaClientData data, boolean isSelf) {

	}
	@Override public void serverTick(CfaAuthoritativeData data) {

	}
	@Override public void travelHook(CfaTravelData data, AquaticActionHelper helper) {
		Submerged.waterMove(data, helper);
		data.setForwardStrafeVel(0, 0);
		data.retrieveStateData(ActionTimerVars.class).actionTimer++;
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(AquaticActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						UnderwaterWalk.ID,
						data -> data.retrieveStateData(ActionTimerVars.class).actionTimer > AQUATIC_STANDUP_TICKS,
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
						(data, isSelf, seed) -> data.storeSound(data.playSound(MarioSFX.AQUATIC_GROUND_POUND_DROP, seed))
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