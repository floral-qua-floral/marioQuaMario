package com.fqf.mario_qua_mario_content.actions.mounted;

import com.fqf.mario_qua_mario_api.definitions.states.actions.GroundedActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.MountedActionDefinition;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.*;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.AnimationHelper;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.PlayermodelAnimation;
import com.fqf.mario_qua_mario_api.definitions.states.actions.util.animation.camera.CameraAnimationSet;
import com.fqf.mario_qua_mario_api.mariodata.IMarioAuthoritativeData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioClientData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioData;
import com.fqf.mario_qua_mario_api.mariodata.IMarioTravelData;
import com.fqf.mario_qua_mario_content.MarioQuaMarioContent;
import com.fqf.mario_qua_mario_content.actions.airborne.Backflip;
import com.fqf.mario_qua_mario_content.actions.grounded.SubWalk;
import net.minecraft.entity.Entity;
import net.minecraft.text.MutableText;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class Mounted implements MountedActionDefinition {
	public static final Identifier ID = MarioQuaMarioContent.makeID("mounted");
	@Override public @NotNull Identifier getID() {
	    return ID;
	}

	@Override public @Nullable PlayermodelAnimation getAnimation(AnimationHelper helper) {
		return null;
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
		return SprintingRule.ALLOW;
	}

	@Override public @Nullable BappingRule getBappingRule() {
		return null;
	}
	@Override public @Nullable Identifier getStompTypeID() {
		return null;
	}

	@Override public @Nullable MutableText dismountingHint() {
		return MarioQuaMarioContent.getClientHelper().getBackflipDismountText();
	}

	@Override public @Nullable Object setupCustomMarioVars(IMarioData data) {
		return null;
	}
	@Override public void clientTick(IMarioClientData data, boolean isSelf) {

	}
	@Override public void serverTick(IMarioAuthoritativeData data) {

	}
	@Override public boolean travelHook(IMarioTravelData data, Entity mount, MountedActionHelper helper) {
		return false;
	}

	@Override public @NotNull List<TransitionDefinition> getBasicTransitions(MountedActionHelper helper) {
		return List.of(
				new TransitionDefinition(
						SubWalk.ID,
						data -> helper.getMount(data) == null,
						EvaluatorEnvironment.COMMON,
						data -> {
							MarioQuaMarioContent.LOGGER.info("Transitioned to SubWalk because mount was null?");
						},
						(data, isSelf, seed) -> {}
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getInputTransitions(MountedActionHelper helper) {
		TransitionDefinition backflip = Backflip.makeBackflipTransition((GroundedActionDefinition.GroundedActionHelper) helper);
		return List.of(
				backflip.variate(
						null,
						data -> data.getInputs().DUCK.isHeld() && data.getInputs().JUMP.isPressed(),
						null,
						data -> {
							helper.dismount(data, false);
							Objects.requireNonNull(backflip.travelExecutor()).execute(data);
						},
						null
				)
		);
	}
	@Override public @NotNull List<TransitionDefinition> getWorldCollisionTransitions(MountedActionHelper helper) {
		return List.of();
	}

	@Override public @NotNull Set<TransitionInjectionDefinition> getTransitionInjections() {
		return Set.of();
	}

	@Override public @NotNull List<AttackInterceptionDefinition> getAttackInterceptions(AnimationHelper animationHelper) {
		return List.of();
	}
}
